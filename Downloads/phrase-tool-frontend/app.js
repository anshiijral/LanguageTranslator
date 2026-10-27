const $ = s => document.querySelector(s);
const esc = s => String(s == null ? "" : s).replace(/[&<>"]/g, c => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" }[c]));

let cat = "ALL";
let editId = 0;
let quiz = null;
let mode = "TYPING";

const toast = m => {
  const t = $("#toast");
  t.textContent = m;
  t.classList.add("show");
  setTimeout(() => t.classList.remove("show"), 2800);
};

const guard = async f => {
  try {
    return await f();
  } catch (e) {
    toast(e.message || "An error occurred");
    return null;
  }
};

// Navigation
document.querySelectorAll("nav button").forEach(b => {
  b.onclick = async () => {
    document.querySelectorAll("nav button").forEach(x => x.classList.toggle("on", x === b));
    ["phrases", "quiz", "progress", "data"].forEach(v => $("#v-" + v).hidden = v !== b.dataset.v);
    if (b.dataset.v === "phrases") await drawPhrases();
    else if (b.dataset.v === "quiz") await drawSetup();
    else if (b.dataset.v === "progress") await drawProgress();
  };
});

// Phrasebook View
async function drawPhrases() {
  await guard(async () => {
    const rawCats = await api.getCategories();
    const cats = ["ALL", ...rawCats];
    $("#chips").innerHTML = cats.map(c =>
      `<button class="chip ${c === cat ? "on" : ""}" data-c="${esc(c)}">${c === "ALL" ? "All situations" : esc(c)}</button>`
    ).join("");

    $("#chips").querySelectorAll(".chip").forEach(b => b.onclick = async () => {
      cat = b.dataset.c;
      await drawPhrases();
    });

    const k = $("#kw").value.trim();
    let list;
    if (k) {
      list = (await api.searchByKeyword(k)).filter(p => cat === "ALL" || p.category.toLowerCase() === cat.toLowerCase());
    } else {
      list = await api.searchByCategory(cat);
    }

    const all = await api.getAllPhrases();
    const verifiedCount = all.filter(p => p.verified).length;
    $("#count").textContent = `${list.length} phrase${list.length === 1 ? "" : "s"} · ${verifiedCount} verified`;

    $("#grid").innerHTML = list.map(p => `
      <article class="card">
        <div class="dev">${esc(p.phrase)}</div>
        ${p.transliteration ? `<div class="tr">${esc(p.transliteration)}</div>` : ""}
        <div class="en">${esc(p.translation)}</div>
        <div class="meta">
          <span class="tag">${esc(p.category)}</span>
          <span class="badge ${p.verified ? "v" : "u"}">${p.verified ? "Verified" : "Unverified"}</span>
        </div>
        <div class="acts">
          <button data-e="${p.id}">Edit</button>
          <button data-v="${p.id}" data-state="${p.verified}">${p.verified ? "Unverify" : "Verify"}</button>
        </div>
      </article>
    `).join("") || '<p class="empty">No phrases match your search.</p>';

    $("#grid").querySelectorAll("[data-e]").forEach(b => b.onclick = () => openDlg(+b.dataset.e));
    $("#grid").querySelectorAll("[data-v]").forEach(b => b.onclick = async () => {
      const id = +b.dataset.v;
      const isVerified = b.dataset.state === "true";
      await guard(async () => {
        await api.setVerified(id, !isVerified);
        toast(`Phrase ${!isVerified ? "marked Verified" : "marked Unverified"}`);
        await drawPhrases();
      });
    });
  });
}

$("#kw").oninput = async () => {
  await drawPhrases();
};

async function openDlg(id) {
  editId = id;
  const all = await api.getAllPhrases();
  const p = id ? all.find(x => x.id === id) : {};
  $("#dtitle").textContent = id ? "Correct phrase" : "Add phrase";
  const f = $("#frm");
  ["phrase", "translation", "transliteration", "category"].forEach(n => f[n].value = p[n] || "");
  $("#derr").textContent = "";

  const cats = await api.getCategories();
  $("#cats").innerHTML = cats.map(c => `<option value="${esc(c)}">`).join("");
  $("#dlg").showModal();
}

$("#addBtn").onclick = () => openDlg(0);
$("#dcancel").onclick = () => $("#dlg").close();

$("#frm").onsubmit = async e => {
  e.preventDefault();
  const f = e.target;
  const p = {
    id: editId,
    phrase: f.phrase.value,
    translation: f.translation.value,
    transliteration: f.transliteration.value,
    category: f.category.value
  };

  try {
    if (editId) {
      await api.correctPhrase(p);
      toast("Phrase corrected successfully");
    } else {
      await api.addPhrase(p);
      toast("Phrase saved as unverified (verify it to practice in quizzes)");
    }
    $("#dlg").close();
    await drawPhrases();
  } catch (x) {
    $("#derr").textContent = x.message;
  }
};

// Quiz View
async function drawSetup() {
  quiz = null;
  const cats = await api.getCategories();
  $("#quizBox").innerHTML = `
    <div class="panel">
      <div class="setup">
        <label>Situation
          <select id="qc">
            <option value="ALL">All situations</option>
            ${cats.map(c => `<option value="${esc(c)}">${esc(c)}</option>`).join("")}
          </select>
        </label>
        <label>Questions
          <input id="qn" type="number" min="1" max="50" value="5">
        </label>
        <div class="seg">
          <button type="button" data-m="TYPING" class="${mode === "TYPING" ? "on" : ""}">Type the answer</button>
          <button type="button" data-m="CHOICE" class="${mode === "CHOICE" ? "on" : ""}">Multiple choice</button>
        </div>
      </div>
      <div class="row end">
        <button class="btn" id="go">Start quiz</button>
      </div>
    </div>
  `;

  document.querySelectorAll(".seg button").forEach(b => b.onclick = () => {
    mode = b.dataset.m;
    document.querySelectorAll(".seg button").forEach(x => x.classList.toggle("on", x === b));
  });

  $("#go").onclick = async () => {
    const selectedCat = $("#qc").value;
    const count = +$("#qn").value;
    const res = await guard(async () => await api.startQuiz(selectedCat, count, mode));
    if (res && res.questions && res.questions.length > 0) {
      quiz = { quizId: res.quizId, qs: res.questions, ans: [], i: 0 };
      drawQ();
    }
  };
}

function drawQ() {
  const { qs, i } = quiz;
  const q = qs[i];
  $("#quizBox").innerHTML = `
    <div class="panel">
      <div class="qtop">
        <span>Question ${i + 1} of ${qs.length}</span>
        <span>${esc(q.category)}</span>
      </div>
      <div class="prog"><i style="width:${((i) / qs.length) * 100}%"></i></div>
      <div class="qtext">${esc(q.text)}</div>
      ${q.translit ? `<div class="qtr">${esc(q.translit)}</div>` : ""}
      ${q.mode === "CHOICE"
        ? `<div class="opts">${(q.options || []).map(o => `<button class="opt" type="button">${esc(o)}</button>`).join("")}</div>`
        : `<div class="row"><input id="ta" placeholder="Type the English meaning" style="flex:1" autocomplete="off"><button class="btn" id="nx" type="button">Next</button></div>`
      }
    </div>
  `;

  const next = async a => {
    quiz.ans.push(a);
    quiz.i++;
    if (quiz.i < qs.length) {
      drawQ();
    } else {
      await finish();
    }
  };

  if (q.mode === "CHOICE") {
    document.querySelectorAll(".opt").forEach(b => b.onclick = () => next(b.textContent));
  } else {
    const input = $("#ta");
    input.focus();
    $("#nx").onclick = () => next(input.value);
    input.onkeydown = e => {
      if (e.key === "Enter") next(input.value);
    };
  }
}

async function finish() {
  const r = await guard(async () => await api.submitQuiz(quiz.quizId, quiz.ans));
  if (!r) return;

  const norm = s => String(s || "").trim().toLowerCase().replace(/\s+/g, " ").replace(/[.?!]+$/, "");
  const bad = quiz.qs.map((q, i) => [q, quiz.ans[i]]).filter(([q, a]) => norm(a) !== norm(q.answer));

  $("#quizBox").innerHTML = `
    <div class="panel">
      <div class="qtop"><span>Quiz complete · Recorded in backend progress</span></div>
      <div class="score">${r.correct}/${r.total} <small style="font-size:22px;color:var(--mute)">(${r.percentage}%)</small></div>
      <div class="wrong">
        ${bad.map(([q, a]) => `
          <div><b class="dev">${esc(q.text)}</b> — you answered “${esc(a || "…")}”, correct: <b>${esc(q.answer)}</b></div>
        `).join("") || "<div style='background:#dcefe3; color: #2f7a55;'>🎉 Perfect score! Every answer was correct.</div>"}
      </div>
      <div class="row end">
        <button class="btn" id="again">New quiz</button>
      </div>
    </div>
  `;
  $("#again").onclick = drawSetup;
}

// Progress View
async function drawProgress() {
  await guard(async () => {
    const s = await api.getProgress();
    const w = await api.getWeakestCategory();

    $("#progBox").innerHTML = s.length ? `
      <table>
        <tr>
          <th>Category</th>
          <th>Quizzes</th>
          <th>Correct / Total</th>
          <th>Accuracy</th>
        </tr>
        ${s.map(x => `
          <tr>
            <td><b>${esc(x.category)}</b></td>
            <td>${x.quizzes}</td>
            <td>${x.correct} / ${x.total}</td>
            <td>
              <div class="row">
                <div class="meter"><i class="${x.accuracy < 60 ? "lo" : ""}" style="width:${Math.min(100, x.accuracy)}%"></i></div>
                ${Number(x.accuracy).toFixed(1)}%
              </div>
            </td>
          </tr>
        `).join("")}
      </table>
      ${w ? `<div class="weak">Weakest category: <b>${esc(w.category)}</b> (${Number(w.accuracy).toFixed(1)}%) — recommended for extra practice!</div>` : ""}
    ` : '<div class="panel empty">No quizzes taken yet. Complete a quiz to track your accuracy!</div>';
  });
}

// CSV Export & Import
$("#exp").onclick = async () => {
  await guard(async () => {
    const csv = await api.exportCsv();
    const a = document.createElement("a");
    a.href = URL.createObjectURL(new Blob(["\ufeff" + csv], { type: "text/csv;charset=utf-8" }));
    a.download = "phrases_export.csv";
    a.click();
    toast("CSV exported successfully");
  });
};

$("#imp").onchange = e => {
  const f = e.target.files[0];
  if (!f) return;
  const r = new FileReader();
  r.onload = async () => {
    await guard(async () => {
      const added = await api.importCsv(r.result);
      toast(`Import complete: ${added} phrase(s) added (duplicates skipped)`);
      await drawPhrases();
    });
  };
  r.readAsText(f, "UTF-8");
  e.target.value = "";
};

// Initial load
drawPhrases();
