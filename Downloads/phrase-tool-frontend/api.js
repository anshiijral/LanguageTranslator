// Data layer: calls the Java PhraseApp backend via REST API
const api = {
  async getAllPhrases() {
    const res = await fetch("/api/phrases");
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.error || "Failed to load phrases");
    }
    return await res.json();
  },

  async getCategories() {
    const res = await fetch("/api/categories");
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.error || "Failed to load categories");
    }
    return await res.json();
  },

  async searchByCategory(category) {
    const res = await fetch("/api/search?c=" + encodeURIComponent(category || "ALL"));
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.error || "Failed to search category");
    }
    return await res.json();
  },

  async searchByKeyword(keyword) {
    const res = await fetch("/api/search?k=" + encodeURIComponent(keyword));
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.error || "Failed to search keyword");
    }
    return await res.json();
  },

  async addPhrase(phrase) {
    const res = await fetch("/api/phrases", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(phrase)
    });
    const data = await res.json().catch(() => ({}));
    if (!res.ok) throw new Error(data.error || "Failed to add phrase");
    return data;
  },

  async correctPhrase(phrase) {
    const res = await fetch("/api/phrases", {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(phrase)
    });
    const data = await res.json().catch(() => ({}));
    if (!res.ok) throw new Error(data.error || "Failed to correct phrase");
    return data;
  },

  async setVerified(id, verified) {
    const res = await fetch("/api/verify", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ id, verified })
    });
    const data = await res.json().catch(() => ({}));
    if (!res.ok) throw new Error(data.error || "Failed to update verification");
    return data;
  },

  async startQuiz(category, count, mode) {
    const res = await fetch("/api/quiz/start", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ category, count, mode })
    });
    const data = await res.json().catch(() => ({}));
    if (!res.ok) throw new Error(data.error || "Failed to start quiz");
    return data; // { quizId, questions }
  },

  async submitQuiz(quizId, answers) {
    const res = await fetch("/api/quiz/submit", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ quizId, answers })
    });
    const data = await res.json().catch(() => ({}));
    if (!res.ok) throw new Error(data.error || "Failed to submit quiz");
    return data;
  },

  async getProgress() {
    const res = await fetch("/api/progress");
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.error || "Failed to load progress");
    }
    return await res.json();
  },

  async getWeakestCategory() {
    const res = await fetch("/api/weakest");
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.error || "Failed to load weakest category");
    }
    return await res.json();
  },

  async exportCsv() {
    const res = await fetch("/api/export");
    if (!res.ok) throw new Error("Failed to export CSV");
    return await res.text();
  },

  async importCsv(csvText) {
    const res = await fetch("/api/import", {
      method: "POST",
      headers: { "Content-Type": "text/plain; charset=utf-8" },
      body: csvText
    });
    const data = await res.json().catch(() => ({}));
    if (!res.ok) throw new Error(data.error || "Failed to import CSV");
    return data.added;
  }
};
