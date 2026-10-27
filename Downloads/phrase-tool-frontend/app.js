const $=s=>document.querySelector(s),esc=s=>String(s).replace(/[&<>"]/g,c=>({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;"}[c]));
let cat="ALL",editId=0,quiz=null,mode="TYPING";
const toast=m=>{const t=$("#toast");t.textContent=m;t.classList.add("show");setTimeout(()=>t.classList.remove("show"),2600)};
const guard=f=>{try{return f()}catch(e){toast(e.message);return null}};
document.querySelectorAll("nav button").forEach(b=>b.onclick=()=>{document.querySelectorAll("nav button").forEach(x=>x.classList.toggle("on",x===b));
 ["phrases","quiz","progress","data"].forEach(v=>$("#v-"+v).hidden=v!==b.dataset.v);({phrases:drawPhrases,quiz:drawSetup,progress:drawProgress}[b.dataset.v]||(()=>{}))()});
function drawPhrases(){const cats=["ALL",...api.getCategories()];
 $("#chips").innerHTML=cats.map(c=>`<button class="chip ${c===cat?"on":""}" data-c="${esc(c)}">${c==="ALL"?"All situations":esc(c)}</button>`).join("");
 $("#chips").querySelectorAll(".chip").forEach(b=>b.onclick=()=>{cat=b.dataset.c;drawPhrases()});
 const k=$("#kw").value;let list=api.searchByCategory(cat);if(k.trim())list=api.searchByKeyword(k).filter(p=>cat==="ALL"||p.category===cat);
 $("#count").textContent=`${list.length} phrase${list.length==1?"":"s"} · ${api.getAllPhrases().filter(p=>p.verified).length} verified`;
 $("#grid").innerHTML=list.map(p=>`<article class="card"><div class="dev">${esc(p.phrase)}</div>${p.transliteration?`<div class="tr">${esc(p.transliteration)}</div>`:""}<div class="en">${esc(p.translation)}</div>
 <div class="meta"><span class="tag">${esc(p.category)}</span><span class="badge ${p.verified?"v":"u"}">${p.verified?"Verified":"Unverified"}</span></div>
 <div class="acts"><button data-e="${p.id}">Edit</button><button data-v="${p.id}">${p.verified?"Unverify":"Verify"}</button></div></article>`).join("")||'<p class="empty">No phrases match your search.</p>';
 $("#grid").querySelectorAll("[data-e]").forEach(b=>b.onclick=()=>openDlg(+b.dataset.e));
 $("#grid").querySelectorAll("[data-v]").forEach(b=>b.onclick=()=>{const p=api.getAllPhrases().find(x=>x.id==b.dataset.v);guard(()=>api.setVerified(p.id,!p.verified));drawPhrases()})}
$("#kw").oninput=()=>guard(drawPhrases)||0;
function openDlg(id){editId=id;const p=id?api.getAllPhrases().find(x=>x.id===id):{};$("#dtitle").textContent=id?"Correct phrase":"Add phrase";const f=$("#frm");
 ["phrase","translation","transliteration","category"].forEach(n=>f[n].value=p[n]||"");$("#derr").textContent="";
 $("#cats").innerHTML=api.getCategories().map(c=>`<option value="${esc(c)}">`).join("");$("#dlg").showModal()}
$("#addBtn").onclick=()=>openDlg(0);$("#dcancel").onclick=()=>$("#dlg").close();
$("#frm").onsubmit=e=>{e.preventDefault();const f=e.target,p={id:editId,phrase:f.phrase.value,translation:f.translation.value,transliteration:f.transliteration.value,category:f.category.value};
 try{editId?api.correctPhrase(p):api.addPhrase(p);$("#dlg").close();toast(editId?"Phrase corrected":"Saved as unverified – verify it to use in quizzes");drawPhrases()}catch(x){$("#derr").textContent=x.message}};
function drawSetup(){quiz=null;const cats=api.getCategories();
 $("#quizBox").innerHTML=`<div class="panel"><div class="setup"><label>Situation<select id="qc"><option value="ALL">All situations</option>${cats.map(c=>`<option>${esc(c)}</option>`).join("")}</select></label>
 <label>Questions<input id="qn" type="number" min="1" value="5"></label>
 <div class="seg"><button data-m="TYPING" class="${mode=="TYPING"?"on":""}">Type the answer</button><button data-m="CHOICE" class="${mode=="CHOICE"?"on":""}">Multiple choice</button></div></div>
 <div class="row end"><button class="btn" id="go">Start quiz</button></div></div>`;
 document.querySelectorAll(".seg button").forEach(b=>b.onclick=()=>{mode=b.dataset.m;document.querySelectorAll(".seg button").forEach(x=>x.classList.toggle("on",x===b))});
 $("#go").onclick=()=>{const qs=guard(()=>api.startQuiz($("#qc").value,+$("#qn").value,mode));if(qs){quiz={qs,ans:[],i:0};drawQ()}}}
function drawQ(){const{qs,i}=quiz,q=qs[i];
 $("#quizBox").innerHTML=`<div class="panel"><div class="qtop"><span>Question ${i+1} of ${qs.length}</span><span>${esc(q.category)}</span></div><div class="prog"><i style="width:${i/qs.length*100}%"></i></div>
 <div class="qtext">${esc(q.text)}</div><div class="qtr">${esc(q.translit)}</div>
 ${q.mode=="CHOICE"?`<div class="opts">${q.options.map(o=>`<button class="opt">${esc(o)}</button>`).join("")}</div>`:`<div class="row"><input id="ta" placeholder="Type the English meaning" style="flex:1"><button class="btn" id="nx">Next</button></div>`}</div>`;
 const next=a=>{quiz.ans.push(a);quiz.i++;quiz.i<qs.length?drawQ():finish()};
 if(q.mode=="CHOICE")document.querySelectorAll(".opt").forEach(b=>b.onclick=()=>next(b.textContent));
 else{$("#ta").focus();$("#nx").onclick=()=>next($("#ta").value);$("#ta").onkeydown=e=>{if(e.key==="Enter")next($("#ta").value)}}}
function finish(){const r=api.submitQuiz(quiz.qs,quiz.ans),n=s=>s.trim().toLowerCase().replace(/\s+/g," ").replace(/[.?!]+$/,"");
 const bad=quiz.qs.map((q,i)=>[q,quiz.ans[i]]).filter(([q,a])=>n(a)!==n(q.answer));
 $("#quizBox").innerHTML=`<div class="panel"><div class="qtop"><span>Quiz complete · saved to progress</span></div><div class="score">${r.correct}/${r.total} <small style="font-size:22px;color:var(--mute)">(${(r.correct*100/r.total).toFixed(1)}%)</small></div>
 <div class="wrong">${bad.map(([q,a])=>`<div><b class="dev">${esc(q.text)}</b> — you wrote “${esc(a||"…")}”, correct: <b>${esc(q.answer)}</b></div>`).join("")||"<div style='background:#dcefe3'>Perfect score. Every answer was right.</div>"}</div>
 <div class="row end"><button class="btn" id="again">New quiz</button></div></div>`;$("#again").onclick=drawSetup}
function drawProgress(){const s=api.getProgress(),w=api.getWeakestCategory();
 $("#progBox").innerHTML=s.length?`<table><tr><th>Category</th><th>Quizzes</th><th>Correct / Total</th><th>Accuracy</th></tr>${s.map(x=>`<tr><td><b>${esc(x.category)}</b></td><td>${x.quizzes}</td><td>${x.correct} / ${x.total}</td><td><div class="row"><div class="meter"><i class="${x.accuracy<60?"lo":""}" style="width:${x.accuracy}%"></i></div>${x.accuracy.toFixed(1)}%</div></td></tr>`).join("")}</table>
 ${w?`<div class="weak">Weakest category: <b>${esc(w.category)}</b> (${w.accuracy.toFixed(1)}%) — worth another round.</div>`:""}`:'<div class="panel empty">No quizzes taken yet.</div>'}
$("#exp").onclick=()=>{const a=document.createElement("a");a.href=URL.createObjectURL(new Blob(["\ufeff"+api.exportCsv()],{type:"text/csv;charset=utf-8"}));a.download="phrases.csv";a.click()};
$("#imp").onchange=e=>{const f=e.target.files[0];if(!f)return;const r=new FileReader();r.onload=()=>{const n=guard(()=>api.importCsv(r.result.replace(/^\ufeff/,"")));if(n!==null)toast(n+" phrase(s) added (duplicates skipped)");drawPhrases()};r.readAsText(f,"UTF-8");e.target.value=""};
drawPhrases();
