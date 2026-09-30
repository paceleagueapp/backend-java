# -*- coding: utf-8 -*-
"""러닝 가이드 정적 페이지 생성기.

content/<lang>.py 의 원고로 web/guide.html, web/guide/*.html (한국어)과
web/<lang>/guide.html, web/<lang>/guide/*.html (그 외 언어)을 만든다.
원고를 고친 뒤 `python scripts/guide-gen/gen.py` 로 다시 생성해서 결과 HTML과 함께 커밋한다.
새 글을 추가하면 web/sitemap.xml 에도 URL을 추가해야 한다.
"""
import importlib, json, os, re, sys

HERE = os.path.dirname(os.path.abspath(__file__))
WEB = os.path.normpath(os.path.join(HERE, "..", "..", "web"))
sys.path.insert(0, HERE)

LANGS = ["ko", "en", "ja", "zh", "es", "fr", "de", "pt", "vi", "th"]
LANG_NAMES = {"ko": "한국어", "en": "English", "ja": "日本語", "zh": "中文", "es": "Español",
              "fr": "Français", "de": "Deutsch", "pt": "Português", "vi": "Tiếng Việt", "th": "ไทย"}
SLUGS = ["pace", "first-5k", "injury-prevention", "landit-strategy"]
DATE = "2026-09-30"
SITE = "https://paceleague.co.kr"


def nav_labels():
    """상단 메뉴 라벨은 web/js/i18n.js 의 TERRITORY_STRINGS 를 그대로 쓴다(한 곳에서만 관리)."""
    src = open(os.path.join(WEB, "js", "i18n.js"), encoding="utf-8").read()
    block = src[src.index("var TERRITORY_STRINGS"):]
    out = {}
    for lang in LANGS:
        line = re.search(r"^\s*%s: \{(.*)$" % lang, block, re.M).group(1)
        out[lang] = dict(re.findall(r"(nav\w+): '([^']*)'", line))
    return out


def base(lang):
    return "" if lang == "ko" else "/" + lang


def hub_path(lang):
    return base(lang) + "/guide"


def art_path(lang, slug):
    return base(lang) + "/guide/" + slug


HEAD = """<!DOCTYPE html>
<html lang="{lang}">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>{title} - Pace League</title>
  <meta name="description" content="{desc}">
  <link rel="canonical" href="{site}{path}">
{alternates}  <link rel="icon" type="image/png" href="/img/favicon.png">
  <script async src="https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js?client=ca-pub-2124974034690240" crossorigin="anonymous"></script>
  <link rel="stylesheet" href="/css/app.css?v=20260908">
  <link rel="stylesheet" href="/css/guide.css?v=20260930b">
  <!-- Google Tag Manager -->
  <script>(function(w,d,s,l,i){{w[l]=w[l]||[];w[l].push({{'gtm.start':
  new Date().getTime(),event:'gtm.js'}});var f=d.getElementsByTagName(s)[0],
  j=d.createElement(s),dl=l!='dataLayer'?'&l='+l:'';j.async=true;j.src=
  'https://www.googletagmanager.com/gtm.js?id='+i+dl;f.parentNode.insertBefore(j,f);
  }})(window,document,'script','dataLayer','GTM-K4Q3GDB8');</script>
  <!-- End Google Tag Manager -->
{jsonld}</head>
<body>
  <!-- Google Tag Manager (noscript) -->
  <noscript><iframe src="https://www.googletagmanager.com/ns.html?id=GTM-K4Q3GDB8"
  height="0" width="0" style="display:none;visibility:hidden"></iframe></noscript>
  <!-- End Google Tag Manager (noscript) -->

  <div class="topbar">
    <div class="topbar-inner">
      <a class="brand" href="/">
        <img class="brand-mark" src="/img/favicon.png" alt="PACELEAGUE">
        <span class="brand-name">PACELEAGUE</span>
      </a>
      <nav class="header-nav">
        <a href="/about" class="navlink" data-navkey="navAbout">{nav[navAbout]}</a>
        <a href="{hub}" class="navlink active" data-navkey="navGuide">{nav[navGuide]}</a>
        <a href="/" class="navlink" data-navkey="navCommunity">{nav[navCommunity]}</a>
        <a href="/territory" class="navlink" data-navkey="navLandit">{nav[navLandit]}</a>
        <a href="/crew" class="navlink" data-navkey="navCrew">{nav[navCrew]}</a>
      </nav>
      <div class="header-actions" id="header-actions"></div>
    </div>
  </div>

  <main class="guide-page">
"""

FOOT = """  </main>

  <nav class="bottom-nav">
    <a href="/" class="bnav" data-navkey="navCommunity">
      <svg width="21" height="21" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
      <span class="bnav-label">{nav[navCommunity]}</span>
    </a>
    <a href="/territory" class="bnav" data-navkey="navLandit">
      <svg width="21" height="21" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 21s-6-5.7-6-10a6 6 0 0 1 12 0c0 4.3-6 10-6 10z"/><circle cx="12" cy="11" r="2"/></svg>
      <span class="bnav-label">{nav[navLandit]}</span>
    </a>
    <a href="/crew" class="bnav" data-navkey="navCrew">
      <svg width="21" height="21" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
      <span class="bnav-label">{nav[navCrew]}</span>
    </a>
  </nav>

  <script src="/js/app.js?v=20260917"></script>
  <script src="/js/i18n.js?v=20260930b"></script>
  <script>
    document.querySelectorAll('[data-navkey]').forEach(function (el) {{
      var key = el.getAttribute('data-navkey');
      var v = t(key);
      if (v && v !== key) {{
        var label = el.querySelector('.bnav-label');
        (label || el).textContent = v;
      }}
    }});
    // 다른 언어로 읽기 링크를 누르면 사이트 언어도 그 언어로 맞춘다
    document.querySelectorAll('.guide-langs a[data-lang]').forEach(function (a) {{
      a.addEventListener('click', function () {{ setLang(a.getAttribute('data-lang')); }});
    }});
    (function renderAuthActions() {{
      var el = document.getElementById('header-actions');
      if (isLoggedIn()) {{
        var nick = localStorage.getItem('pl_nickname') || '';
        el.innerHTML = '<span class="header-nick">' + escapeHtml(nick) + '</span>'
          + '<button class="btn ghost" id="logout-btn">' + t('logout') + '</button>';
        document.getElementById('logout-btn').addEventListener('click', logout);
      }} else {{
        el.innerHTML = '<a class="btn" href="/login">' + t('login') + '</a>';
      }}
    }})();
  </script>
</body>
</html>
"""


def alternates(path_fn):
    lines = ['  <link rel="alternate" hreflang="%s" href="%s%s">\n' % (l, SITE, path_fn(l)) for l in LANGS]
    lines.append('  <link rel="alternate" hreflang="x-default" href="%s%s">\n' % (SITE, path_fn("ko")))
    return "".join(lines)


def lang_row(cur, path_fn, label):
    links = []
    for l in LANGS:
        if l == cur:
            links.append("<strong>%s</strong>" % LANG_NAMES[l])
        else:
            links.append('<a href="%s" hreflang="%s" data-lang="%s">%s</a>' % (path_fn(l), l, l, LANG_NAMES[l]))
    return '    <div class="guide-langs">🌐 %s: %s</div>\n' % (label, " · ".join(links))


def jsonld(lang, slug, a):
    data = {
        "@context": "https://schema.org", "@type": "Article",
        "headline": a["title"], "description": a["desc"],
        "datePublished": DATE, "dateModified": DATE, "inLanguage": lang,
        "mainEntityOfPage": SITE + art_path(lang, slug),
        "author": {"@type": "Organization", "name": "Pace League", "url": SITE + "/"},
        "publisher": {"@type": "Organization", "name": "Pace League",
                      "logo": {"@type": "ImageObject", "url": SITE + "/img/favicon.png"}},
    }
    return '  <script type="application/ld+json">' + json.dumps(data, ensure_ascii=False) + "</script>\n"


def write(rel, content):
    p = os.path.join(WEB, *rel.strip("/").split("/")) + ".html"
    os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, "w", encoding="utf-8", newline="\n") as f:
        f.write(content)


def main():
    navs = nav_labels()
    for lang in LANGS:
        c = importlib.import_module("content." + lang)
        ui, nav = c.UI, navs[lang]
        for slug in SLUGS:
            a = c.ARTICLES[slug]
            html = HEAD.format(lang=lang, title=a["title"], desc=a["desc"], site=SITE, path=art_path(lang, slug),
                               alternates=alternates(lambda l: art_path(l, slug)), jsonld=jsonld(lang, slug, a),
                               nav=nav, hub=hub_path(lang))
            html += '    <div class="guide-crumb"><a href="%s">%s</a> › %s</div>\n' % (hub_path(lang), ui["crumb"], a["tag"])
            html += "    <h1>%s</h1>\n" % a["title"]
            html += '    <div class="guide-meta">%s · %s</div>\n' % (ui["author"], ui["date"])
            html += lang_row(lang, lambda l: art_path(l, slug), ui["read_in"])
            html += c.BODY[slug]
            related = "\n".join('      <a href="%s">%s</a>' % (art_path(lang, s), c.ARTICLES[s]["title"])
                                for s in SLUGS if s != slug)
            html += '\n    <div class="guide-related">\n      <h2>%s</h2>\n%s\n    </div>\n' % (ui["related"], related)
            html += FOOT.format(nav=nav)
            write(art_path(lang, slug), html)

        cards = "\n".join("""      <a class="guide-card" href="%s">
        <div class="guide-card-tag">%s</div>
        <h2>%s</h2>
        <p>%s</p>
      </a>""" % (art_path(lang, s), c.ARTICLES[s]["tag"], c.ARTICLES[s]["title"], c.ARTICLES[s]["summary"]) for s in SLUGS)
        hub = HEAD.format(lang=lang, title=c.HUB["title"], desc=c.HUB["desc"], site=SITE, path=hub_path(lang),
                          alternates=alternates(hub_path), jsonld="", nav=nav, hub=hub_path(lang))
        hub += "    <h1>%s</h1>\n" % c.HUB["title"]
        hub += lang_row(lang, hub_path, ui["read_in"])
        hub += '    <p class="guide-lead">%s</p>\n' % c.HUB["lead"]
        hub += '    <div class="guide-list">\n%s\n    </div>\n' % cards
        hub += FOOT.format(nav=nav)
        write(hub_path(lang), hub)
        print("generated", lang)


if __name__ == "__main__":
    main()
