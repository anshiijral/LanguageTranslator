// Data layer: same method names as the Java PhraseApp facade. Swap the bodies for fetch() calls to a REST wrapper later.
const SEED=[["Greetings",[["नमस्ते","namaste","Hello"],["आप कैसे हैं?","aap kaise hain?","How are you?"],["मैं ठीक हूँ","main theek hoon","I am fine"],["धन्यवाद","dhanyavaad","Thank you"],["सुप्रभात","suprabhaat","Good morning"],["शुभ रात्रि","shubh raatri","Good night"],["आपका नाम क्या है?","aapka naam kya hai?","What is your name?"]]],
["Travel",[["बस स्टॉप कहाँ है?","bus stop kahaan hai?","Where is the bus stop?"],["रेलवे स्टेशन कहाँ है?","railway station kahaan hai?","Where is the railway station?"],["सीधे जाइए","seedhe jaaiye","Go straight"],["दाएँ मुड़िए","daayen mudiye","Turn right"],["बाएँ मुड़िए","baayen mudiye","Turn left"],["कृपया रुकिए","kripya rukiye","Please stop"],["यह कहाँ है?","yah kahaan hai?","Where is this?"]]],
["Food",[["मुझे पानी चाहिए","mujhe paani chaahiye","I need water"],["मुझे खाना चाहिए","mujhe khaana chaahiye","I want food"],["कृपया चाय दीजिए","kripya chaay dijiye","Please give tea"],["यह बहुत स्वादिष्ट है","yah bahut swaadisht hai","It is very tasty"],["थोड़ा नमक चाहिए","thoda namak chaahiye","I need a little salt"],["बिल दीजिए","bill dijiye","Please give the bill"],["मुझे भूख लगी है","mujhe bhookh lagi hai","I am hungry"]]],
["Shopping",[["यह कितने का है?","yah kitne ka hai?","How much is this?"],["थोड़ा कम कीजिए","thoda kam kijiye","Please reduce the price a little"],["मुझे यह नहीं चाहिए","mujhe yah nahin chaahiye","I do not want this"],["दूसरा दिखाइए","doosra dikhaaiye","Show me another one"],["थैला दीजिए","thaila dijiye","Please give a bag"],["मुझे यह पसंद आया","mujhe yah pasand aaya","I liked this"],["यह अच्छा है","yah achchha hai","This is good"]]],
["Emergency",[["मदद कीजिए!","madad kijiye!","Help me!"],["पुलिस को बुलाइए","police ko bulaaiye","Call the police"],["डॉक्टर को बुलाइए","doctor ko bulaaiye","Call a doctor"],["मेरी तबियत ठीक नहीं है","meri tabiyat theek nahin hai","I am not well"],["मैं रास्ता भूल गया हूँ","main raasta bhool gaya hoon","I am lost"],["अस्पताल कहाँ है?","aspataal kahaan hai?","Where is the hospital?"],["आग!","aag!","Fire!"]]]];
const ld=(k,d)=>{try{return JSON.parse(localStorage.getItem(k))||d}catch(e){return d}};
let P=ld("pt_phrases",null),R=ld("pt_results",[]);
if(!P){P=[];let id=1;SEED.forEach(([c,l])=>l.forEach(([p,t,e])=>P.push({id:id++,phrase:p,transliteration:t,translation:e,category:c,verified:true})));}
const save=()=>{localStorage.setItem("pt_phrases",JSON.stringify(P));localStorage.setItem("pt_results",JSON.stringify(R))};save();
const low=s=>(s||"").trim().toLowerCase(),norm=s=>low(s).replace(/\s+/g," ").replace(/[.?!]+$/,"");
const fail=(n,m)=>{const e=new Error(m);e.name=n;throw e};
function clean(p){const o={phrase:(p.phrase||"").trim(),translation:(p.translation||"").trim(),transliteration:(p.transliteration||"").trim(),category:(p.category||"").trim()};
 if(!o.phrase)fail("InvalidInput","Phrase text is required");if(!o.translation)fail("InvalidInput","Translation is required");if(!o.category)fail("InvalidInput","Category is required");
 if(o.phrase.length>200||o.translation.length>200||o.transliteration.length>200)fail("InvalidInput","Text is limited to 200 characters");
 if(o.category.length>50)fail("InvalidInput","Category is limited to 50 characters");return o}
const dup=(t,id)=>P.some(x=>x.id!==id&&low(x.phrase)===low(t));
const shuf=a=>{for(let i=a.length-1;i>0;i--){const j=Math.floor(Math.random()*(i+1));[a[i],a[j]]=[a[j],a[i]]}return a};
const api={
 getAllPhrases:()=>[...P].sort((a,b)=>a.category.localeCompare(b.category)||a.id-b.id),
 getCategories:()=>[...new Set(P.map(p=>p.category))].sort(),
 searchByCategory:c=>!c||low(c)==="all"?api.getAllPhrases():api.getAllPhrases().filter(p=>low(p.category)===low(c)),
 searchByKeyword(k){if(!low(k))fail("InvalidInput","Keyword cannot be empty");k=low(k);return api.getAllPhrases().filter(p=>[p.phrase,p.translation,p.transliteration].some(s=>low(s).includes(k)))},
 addPhrase(p){const o=clean(p);if(dup(o.phrase,0))fail("Duplicate","Phrase already exists: "+o.phrase);P.push({id:Math.max(0,...P.map(x=>x.id))+1,...o,verified:false});save()},
 correctPhrase(p){const o=clean(p),old=P.find(x=>x.id===p.id);if(!old)fail("NotFound","No phrase with id "+p.id);if(dup(o.phrase,p.id))fail("Duplicate","Phrase already exists: "+o.phrase);Object.assign(old,o);save()},
 setVerified(id,v){const p=P.find(x=>x.id===id);if(!p)fail("NotFound","No phrase with id "+id);p.verified=v;save()},
 startQuiz(cat,count,mode){mode=(mode||"").toUpperCase();if(!["TYPING","CHOICE"].includes(mode)||!(count>=1))fail("InvalidInput","Choose a mode and at least 1 question");
  const all=P.filter(p=>p.verified),pool=all.filter(p=>!cat||low(cat)==="all"||low(p.category)===low(cat));
  if(!pool.length)fail("InvalidInput","No verified phrases found for category "+cat);if(mode==="CHOICE"&&all.length<2)fail("InvalidInput","Need at least 2 verified phrases");
  return shuf([...pool]).slice(0,Math.min(count,pool.length)).map(p=>{const q={mode,text:p.phrase,translit:p.transliteration,category:p.category,answer:p.translation};
   if(mode==="CHOICE"){const w=[];all.forEach(x=>{if(low(x.translation)!==low(p.translation)&&!w.some(y=>low(y)===low(x.translation)))w.push(x.translation)});q.options=shuf([...shuf(w).slice(0,3),p.translation])}return q})},
 submitQuiz(qs,ans){if(!qs||!ans||qs.length!==ans.length)fail("InvalidInput","Answers do not match questions");
  const cats=new Set(qs.map(q=>q.category)),r={date:new Date().toISOString().slice(0,10),category:cats.size===1?[...cats][0]:"ALL",correct:qs.filter((q,i)=>norm(ans[i])===norm(q.answer)).length,total:qs.length};R.push(r);save();return r},
 getProgress(){const m={};R.forEach(r=>{const a=m[r.category]||(m[r.category]={category:r.category,quizzes:0,correct:0,total:0});a.quizzes++;a.correct+=r.correct;a.total+=r.total});
  return Object.values(m).sort((a,b)=>a.category.localeCompare(b.category)).map(s=>({...s,accuracy:s.total?s.correct*100/s.total:0}))},
 getWeakestCategory(){let w=null;api.getProgress().forEach(s=>{if(s.category!=="ALL"&&s.total&&(!w||s.accuracy<w.accuracy))w=s});return w},
 exportCsv(){const q=s=>'"'+String(s).replace(/"/g,'""')+'"';return"id,phrase,translation,transliteration,category,verified\n"+api.getAllPhrases().map(p=>[p.id,p.phrase,p.translation,p.transliteration,p.category,p.verified].map(q).join(",")).join("\n")},
 importCsv(txt){let n=0;txt.split(/\r?\n/).slice(1).filter(l=>l.trim()).forEach(l=>{const c=[];let cur="",inq=false;for(let i=0;i<l.length;i++){const ch=l[i];if(ch==='"'){if(inq&&l[i+1]==='"'){cur+='"';i++}else inq=!inq}else if(ch===","&&!inq){c.push(cur);cur=""}else cur+=ch}c.push(cur);
  if(c.length<6)fail("InvalidInput","Bad CSV format");try{api.addPhrase({phrase:c[1],translation:c[2],transliteration:c[3],category:c[4]});n++}catch(e){}});return n}};
