// 크루 페이지 (crew.html). app.js / i18n.js 다음에 로드.
// GET /api/crew/me 로 분기: 크루 없으면 검색/생성/받은초대, 있으면 크루 정보(+크루장이면 관리 패널).

(function () {
  var viewEl = document.getElementById('view');

  // 로그인 안 했으면 크루 기능을 못 쓰므로 로그인으로 보낸다.
  if (redirectToLoginIfNeeded()) return;

  renderAuthActions();
  renderLangSelect(document.getElementById('lang-select'));
  ['nav-community:navCommunity', 'nav-landit:navLandit', 'nav-crew:navCrew'].forEach(function (p) {
    var id = p.split(':')[0], key = p.split(':')[1];
    var el = document.getElementById(id), v = t(key);
    if (el && v && v !== key) el.textContent = v;
  });

  loadMyCrew();

  // ── helpers ──────────────────────────────────────────────────────────────
  function renderAuthActions() {
    var el = document.getElementById('auth-actions');
    var nick = localStorage.getItem('pl_nickname') || '';
    el.innerHTML = '<span style="color:#ccc;font-size:13px;margin-right:8px;">' + escapeHtml(nick) + '</span>'
      + '<button class="btn ghost sm" id="logout-btn">' + t('logout') + '</button>';
    document.getElementById('logout-btn').addEventListener('click', logout);
  }

  function iconColor(key) {
    var h = 0;
    key = key || '';
    for (var i = 0; i < key.length; i++) h = (h * 31 + key.charCodeAt(i)) % 360;
    return 'hsl(' + h + ', 55%, 45%)';
  }

  function crewIconHtml(name, iconUrl) {
    if (iconUrl) return '<img class="crew-icon" src="' + escapeHtml(iconUrl) + '" alt="">';
    var ch = (name || '?').trim().charAt(0) || '?';
    return '<div class="crew-icon" style="background:' + iconColor(name) + '">' + escapeHtml(ch) + '</div>';
  }

  function setMsg(el, text, kind) {
    el.textContent = text || '';
    el.className = 'msg' + (kind ? ' ' + kind : '');
  }

  // 이미지 1장 업로드 → APPROVED media sno 반환(Promise). 이미지는 complete가 동기라 폴링 불필요.
  function uploadIcon(file) {
    return apiFetch('/api/media/uploads', {
      method: 'POST',
      body: { type: 'IMAGE', mimeType: file.type, fileSizeBytes: file.size }
    }).then(function (json) {
      if (!json || !json.success || !json.data) throw new Error('upload init failed');
      var mediaSno = json.data.mediaSno;
      return fetch(json.data.uploadUrl, { method: 'PUT', headers: { 'Content-Type': file.type }, body: file })
        .then(function (res) {
          if (!res.ok) throw new Error('S3 PUT failed');
          return apiFetch('/api/media/' + mediaSno + '/complete', { method: 'POST', body: {} });
        })
        .then(function (cj) {
          if (!cj || !cj.success || !cj.data) throw new Error('complete failed');
          if (cj.data.status !== 'APPROVED') throw new Error('이미지가 거부되었습니다');
          return mediaSno;
        });
    });
  }

  function loadMyCrew() {
    apiFetch('/api/crew/me?lang=' + encodeURIComponent(getLang())).then(function (json) {
      if (!json || !json.success) { viewEl.innerHTML = '<p class="muted">불러오지 못했습니다.</p>'; return; }
      if (json.data) renderCrewView(json.data);
      else renderNoCrewView();
    });
  }

  // ── 크루 없는 회원: 검색 + 생성 + 받은 초대 ────────────────────────────────
  function renderNoCrewView() {
    viewEl.innerHTML =
      '<div class="card"><h2>' + t('crewInvitesTitle') + '</h2><div id="inv-list"><p class="muted">-</p></div></div>' +
      '<div class="card"><h2>' + t('crewSearchTitle') + '</h2>' +
        '<input type="search" id="crew-q" placeholder="' + t('crewSearchPlaceholder') + '">' +
        '<div id="crew-results"></div></div>' +
      '<div class="card"><h2>' + t('crewCreateTitle') + '</h2>' +
        '<input type="text" id="c-name" placeholder="' + t('crewNamePlaceholder') + '" maxlength="20">' +
        '<textarea id="c-desc" placeholder="' + t('crewDescPlaceholder') + '" maxlength="500"></textarea>' +
        '<div class="row" style="margin-bottom:8px;"><input type="file" id="c-icon" accept="image/*">' +
        '<span class="muted" id="c-icon-msg"></span></div>' +
        '<button class="btn" id="c-submit">' + t('crewCreateBtn') + '</button>' +
        '<div class="msg" id="c-msg"></div></div>';

    loadInvitations();

    var q = document.getElementById('crew-q');
    var results = document.getElementById('crew-results');
    var timer = null;
    q.addEventListener('input', function () {
      clearTimeout(timer);
      timer = setTimeout(function () { searchCrews(q.value, results); }, 300);
    });
    searchCrews('', results);

    document.getElementById('c-submit').addEventListener('click', createCrew);
  }

  function loadInvitations() {
    apiFetch('/api/crew/invitations/me').then(function (json) {
      var el = document.getElementById('inv-list');
      var items = (json && json.data) || [];
      if (!items.length) { el.innerHTML = '<p class="muted">' + t('crewNoInvites') + '</p>'; return; }
      el.innerHTML = '<ul class="list">' + items.map(function (iv) {
        return '<li>' + crewIconHtml(iv.crewName, iv.crewIconUrl) +
          '<div class="grow"><div class="nick">' + escapeHtml(iv.crewName) + '</div>' +
          '<div class="muted">' + escapeHtml(iv.inviterNickname) + '</div></div>' +
          '<button class="btn sm" data-acc="' + iv.id + '">' + t('crewAccept') + '</button> ' +
          '<button class="btn ghost sm" data-dec="' + iv.id + '">' + t('crewDecline') + '</button></li>';
      }).join('') + '</ul>';
      el.querySelectorAll('[data-acc]').forEach(function (b) {
        b.addEventListener('click', function () {
          apiFetch('/api/crew/invitations/' + b.getAttribute('data-acc') + '/accept', { method: 'POST', body: {} })
            .then(handleActionResult);
        });
      });
      el.querySelectorAll('[data-dec]').forEach(function (b) {
        b.addEventListener('click', function () {
          apiFetch('/api/crew/invitations/' + b.getAttribute('data-dec') + '/decline', { method: 'POST', body: {} })
            .then(function () { loadInvitations(); });
        });
      });
    });
  }

  function searchCrews(query, el) {
    apiFetch('/api/crew/search?lang=' + encodeURIComponent(getLang()) + '&q=' + encodeURIComponent(query || ''))
      .then(function (json) {
        var items = (json && json.data) || [];
        if (!items.length) { el.innerHTML = '<p class="muted">' + t('crewNoResults') + '</p>'; return; }
        el.innerHTML = '<ul class="list">' + items.map(function (c) {
          var full = c.memberCount >= c.memberLimit;
          return '<li>' + crewIconHtml(c.name, c.iconUrl) +
            '<div class="grow"><div class="nick">' + escapeHtml(c.name) + '</div>' +
            '<div class="muted">' + escapeHtml(c.description || '') + '</div>' +
            '<div class="muted">' + c.memberCount + ' / ' + c.memberLimit + '</div></div>' +
            '<button class="btn sm" data-apply="' + c.sno + '"' + (full ? ' disabled' : '') + '>' +
            t('crewApply') + '</button></li>';
        }).join('') + '</ul>';
        el.querySelectorAll('[data-apply]').forEach(function (b) {
          b.addEventListener('click', function () {
            b.disabled = true;
            apiFetch('/api/crew/' + b.getAttribute('data-apply') + '/join-requests', { method: 'POST', body: { message: '' } })
              .then(handleActionResult);
          });
        });
      });
  }

  function createCrew() {
    var name = document.getElementById('c-name').value.trim();
    var desc = document.getElementById('c-desc').value.trim();
    var fileInput = document.getElementById('c-icon');
    var msg = document.getElementById('c-msg');
    var btn = document.getElementById('c-submit');
    if (!name) { setMsg(msg, t('crewNameRequired'), 'err'); return; }
    btn.disabled = true;
    setMsg(msg, t('crewCreating'), '');

    var iconP = (fileInput.files && fileInput.files[0])
      ? uploadIcon(fileInput.files[0]).catch(function (e) { throw e; })
      : Promise.resolve(null);

    iconP.then(function (iconMediaId) {
      return apiFetch('/api/crew', { method: 'POST', body: { name: name, iconMediaId: iconMediaId, description: desc } });
    }).then(function (json) {
      if (json && json.success) { location.reload(); }
      else { setMsg(msg, (json && json.message) || t('crewCreateFailed'), 'err'); btn.disabled = false; }
    }).catch(function (e) {
      setMsg(msg, e.message || t('crewCreateFailed'), 'err'); btn.disabled = false;
    });
  }

  function handleActionResult(json) {
    if (json && json.success) location.reload();
    else alert((json && json.message) || '처리에 실패했습니다.');
  }

  // ── 크루원: 크루 정보 페이지 ──────────────────────────────────────────────
  function renderCrewView(crew) {
    var myMemberSno = getMemberSno();
    var html =
      '<div class="card"><div class="row" style="align-items:flex-start;">' +
        crewIconHtml(crew.name, crew.iconUrl) +
        '<div class="grow"><div class="nick" style="font-size:18px;">' + escapeHtml(crew.name) + '</div>' +
        '<div class="muted">' + escapeHtml(crew.description || '') + '</div>' +
        '<div class="muted">' + crew.memberCount + ' / ' + crew.memberLimit + '</div></div></div></div>';

    html += '<div class="card"><h2>' + t('crewNoticeTitle') + '</h2>' +
      '<div class="notice-box">' + (crew.notice ? escapeHtml(crew.notice) : '<span class="muted">' + t('crewNoNotice') + '</span>') + '</div></div>';

    html += '<div class="card"><h2>' + t('crewMembersTitle') + ' (' + crew.members.length + ')</h2><ul class="list">' +
      crew.members.map(function (m) {
        var isLeader = m.role === 'LEADER';
        var canKick = crew.viewerIsLeader && !isLeader;
        return '<li><div class="grow"><span class="nick">' + escapeHtml(m.nickname) + '</span> ' +
          '<span class="tier-badge">' + escapeHtml(m.tierLabel || m.tier || '') + '</span>' +
          (isLeader ? ' <span class="leader-badge">' + t('crewLeaderBadge') + '</span>' : '') + '</div>' +
          (canKick ? '<button class="btn ghost sm" data-kick="' + m.memberSno + '">' + t('crewKick') + '</button> ' +
                     '<button class="btn ghost sm" data-transfer="' + m.memberSno + '">' + t('crewTransfer') + '</button>' : '') +
          '</li>';
      }).join('') + '</ul></div>';

    html += '<div class="card"><h2>' + t('crewRankingTitle') + '</h2><div id="crew-ranking"><p class="muted">-</p></div></div>';

    if (crew.viewerIsLeader) {
      html += leaderPanelHtml(crew);
    } else {
      html += '<div class="card"><button class="btn ghost" id="leave-btn">' + t('crewLeave') + '</button></div>';
    }

    viewEl.innerHTML = html;
    loadCrewRanking(crew.sno);

    viewEl.querySelectorAll('[data-kick]').forEach(function (b) {
      b.addEventListener('click', function () {
        if (!confirm(t('crewKickConfirm'))) return;
        apiFetch('/api/crew/' + crew.sno + '/members/' + b.getAttribute('data-kick'), { method: 'DELETE' }).then(handleActionResult);
      });
    });
    viewEl.querySelectorAll('[data-transfer]').forEach(function (b) {
      b.addEventListener('click', function () {
        if (!confirm(t('crewTransferConfirm'))) return;
        apiFetch('/api/crew/' + crew.sno + '/leader', { method: 'POST', body: { inviteeMemberSno: Number(b.getAttribute('data-transfer')) } }).then(handleActionResult);
      });
    });
    var leaveBtn = document.getElementById('leave-btn');
    if (leaveBtn) leaveBtn.addEventListener('click', function () {
      if (!confirm(t('crewLeaveConfirm'))) return;
      apiFetch('/api/crew/' + crew.sno + '/members/me', { method: 'DELETE' }).then(handleActionResult);
    });

    if (crew.viewerIsLeader) wireLeaderPanel(crew);
  }

  function loadCrewRanking(crewSno) {
    apiFetch('/api/crew/' + crewSno + '/ranking?lang=' + encodeURIComponent(getLang())).then(function (json) {
      var el = document.getElementById('crew-ranking');
      if (!el) return;
      var items = (json && json.data) || [];
      if (!items.length) { el.innerHTML = '<p class="muted">-</p>'; return; }
      el.innerHTML = '<ul class="list">' + items.map(function (r) {
        return '<li><span class="top10-rank" style="width:20px;color:#e53935;font-weight:700;flex-shrink:0;">' + r.rank + '</span>' +
          '<div class="grow"><span class="nick">' + escapeHtml(r.nickname) + '</span> ' +
          '<span class="tier-badge">' + escapeHtml(r.tierLabel || r.tier || '') + '</span>' +
          (r.isLeader ? ' <span class="leader-badge">' + t('crewLeaderBadge') + '</span>' : '') + '</div>' +
          '<span style="font-weight:600;flex-shrink:0;">' + r.totalScore + '</span></li>';
      }).join('') + '</ul>';
    });
  }

  function leaderPanelHtml(crew) {
    return '<div class="card"><h2>' + t('crewManageTitle') + '</h2>' +
      '<h2 style="font-size:13px;margin-top:8px;">' + t('crewInviteTitle') + '</h2>' +
      '<input type="search" id="m-q" placeholder="' + t('crewInvitePlaceholder') + '">' +
      '<div id="m-results"></div>' +
      '<h2 style="font-size:13px;margin-top:16px;">' + t('crewJoinReqTitle') + '</h2>' +
      '<div id="jr-list"><p class="muted">-</p></div>' +
      '<h2 style="font-size:13px;margin-top:16px;">' + t('crewEditTitle') + '</h2>' +
      '<input type="text" id="e-name" maxlength="20" value="' + escapeHtml(crew.name) + '">' +
      '<textarea id="e-desc" maxlength="500" placeholder="' + t('crewDescPlaceholder') + '">' + escapeHtml(crew.description || '') + '</textarea>' +
      '<textarea id="e-notice" maxlength="2000" placeholder="' + t('crewNoticePlaceholder') + '">' + escapeHtml(crew.notice || '') + '</textarea>' +
      '<div class="row" style="margin-bottom:8px;"><input type="file" id="e-icon" accept="image/*"><span class="muted" id="e-icon-msg"></span></div>' +
      '<button class="btn" id="e-save">' + t('saveLabel') + '</button>' +
      '<div class="msg" id="e-msg"></div>' +
      '<hr style="border-color:#262626;margin:16px 0;">' +
      '<button class="btn ghost" id="disband-btn">' + t('crewDisband') + '</button></div>';
  }

  function wireLeaderPanel(crew) {
    var q = document.getElementById('m-q');
    var results = document.getElementById('m-results');
    var timer = null;
    q.addEventListener('input', function () {
      clearTimeout(timer);
      timer = setTimeout(function () {
        if (!q.value.trim()) { results.innerHTML = ''; return; }
        apiFetch('/api/member/search?q=' + encodeURIComponent(q.value.trim())).then(function (json) {
          var items = (json && json.data) || [];
          results.innerHTML = items.length ? '<ul class="list">' + items.map(function (m) {
            return '<li><div class="grow"><span class="nick">' + escapeHtml(m.nickname) + '</span> ' +
              '<span class="muted">' + escapeHtml(m.memberId) + '</span></div>' +
              '<button class="btn sm" data-invite="' + m.memberSno + '">' + t('crewInviteBtn') + '</button></li>';
          }).join('') + '</ul>' : '<p class="muted">' + t('crewNoResults') + '</p>';
          results.querySelectorAll('[data-invite]').forEach(function (b) {
            b.addEventListener('click', function () {
              b.disabled = true;
              apiFetch('/api/crew/' + crew.sno + '/invitations', { method: 'POST', body: { inviteeMemberSno: Number(b.getAttribute('data-invite')) } })
                .then(function (json) {
                  if (json && json.success) { b.textContent = t('crewInvited'); }
                  else { b.disabled = false; alert((json && json.message) || '초대 실패'); }
                });
            });
          });
        });
      }, 300);
    });

    loadJoinRequests(crew.sno);

    document.getElementById('e-save').addEventListener('click', function () { saveCrew(crew); });
    document.getElementById('disband-btn').addEventListener('click', function () {
      if (!confirm(t('crewDisbandConfirm'))) return;
      apiFetch('/api/crew/' + crew.sno, { method: 'DELETE' }).then(handleActionResult);
    });
  }

  function loadJoinRequests(crewSno) {
    apiFetch('/api/crew/' + crewSno + '/join-requests').then(function (json) {
      var el = document.getElementById('jr-list');
      var items = (json && json.data) || [];
      if (!items.length) { el.innerHTML = '<p class="muted">' + t('crewNoJoinReq') + '</p>'; return; }
      el.innerHTML = '<ul class="list">' + items.map(function (r) {
        return '<li><div class="grow"><span class="nick">' + escapeHtml(r.nickname) + '</span>' +
          (r.message ? '<div class="muted">' + escapeHtml(r.message) + '</div>' : '') + '</div>' +
          '<button class="btn sm" data-appr="' + r.id + '">' + t('crewApprove') + '</button> ' +
          '<button class="btn ghost sm" data-rej="' + r.id + '">' + t('crewReject') + '</button></li>';
      }).join('') + '</ul>';
      el.querySelectorAll('[data-appr]').forEach(function (b) {
        b.addEventListener('click', function () {
          apiFetch('/api/crew/join-requests/' + b.getAttribute('data-appr') + '/approve', { method: 'POST', body: {} }).then(handleActionResult);
        });
      });
      el.querySelectorAll('[data-rej]').forEach(function (b) {
        b.addEventListener('click', function () {
          apiFetch('/api/crew/join-requests/' + b.getAttribute('data-rej') + '/reject', { method: 'POST', body: {} })
            .then(function () { loadJoinRequests(crewSno); });
        });
      });
    });
  }

  function saveCrew(crew) {
    var name = document.getElementById('e-name').value.trim();
    var desc = document.getElementById('e-desc').value.trim();
    var notice = document.getElementById('e-notice').value.trim();
    var fileInput = document.getElementById('e-icon');
    var msg = document.getElementById('e-msg');
    var btn = document.getElementById('e-save');
    btn.disabled = true;
    setMsg(msg, t('crewSaving'), '');

    var iconP = (fileInput.files && fileInput.files[0])
      ? uploadIcon(fileInput.files[0])
      : Promise.resolve(null);

    iconP.then(function (newMediaId) {
      var body = { name: name, description: desc, notice: notice, iconMediaId: newMediaId };
      // 새 이미지를 안 골랐으면 현재 아이콘 URL을 그대로 돌려보내 유지시킨다.
      if (newMediaId == null) body.iconUrl = crew.iconUrl || null;
      return apiFetch('/api/crew/' + crew.sno, { method: 'PUT', body: body });
    }).then(function (json) {
      if (json && json.success) location.reload();
      else { setMsg(msg, (json && json.message) || t('crewSaveFailed'), 'err'); btn.disabled = false; }
    }).catch(function (e) { setMsg(msg, e.message || t('crewSaveFailed'), 'err'); btn.disabled = false; });
  }
})();
