// PaceLeague 공유 스크립트 — index.html(게시판 피드)/login.html/post.html에서 공통으로 사용.

var API_BASE = 'https://api.paceleague.co.kr';

var STORAGE_KEYS = {
  accessToken: 'pl_access_token',
  refreshToken: 'pl_refresh_token',
  nickname: 'pl_nickname',
  memberSno: 'pl_member_sno'
};

function escapeHtml(str) {
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

// 서버는 createAt/updateAt을 UTC 기준 시각으로 보내지만 "Z"(타임존 표시)가 없는 문자열이라,
// 그냥 new Date()에 넘기면 브라우저가 "이미 내 로컬 시간"으로 잘못 해석한다.
// UTC임을 명시해준 뒤 보는 사람의 브라우저 타임존/로케일로 자동 변환해서 표시한다.
function formatLocalTime(isoString) {
  if (!isoString) return '';
  var withZone = /Z$|[+-]\d\d:\d\d$/.test(isoString) ? isoString : isoString + 'Z';
  var date = new Date(withZone);
  if (isNaN(date.getTime())) return isoString;
  return date.toLocaleString(undefined, {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  });
}

// record의 startTime/endTime은 createAt/updateAt과 달리 "클라이언트가 기록한 그대로의 로컬 시각"이라
// 타임존 정보가 없다(서버는 저장만 함, utcOffset은 참고용). formatLocalTime처럼 UTC로 간주해 변환하면
// 오히려 왜곡되므로, 문자열을 그대로 "yyyy-MM-dd HH:mm"으로만 잘라 보여준다.
function formatRecordDateTime(isoString) {
  if (!isoString) return '';
  var m = String(isoString).match(/^(\d{4}-\d{2}-\d{2})T(\d{2}:\d{2})/);
  return m ? (m[1] + ' ' + m[2]) : isoString;
}

function formatDistanceKm(meters) {
  if (meters == null) return '';
  return (Number(meters) / 1000).toFixed(2) + 'km';
}

// startTime/endTime 둘 다 타임존 없이 같은 방식으로 해석되므로, 절대 시각이 아닌 "차이(소요시간)"만
// 구하는 이 계산은 브라우저 로컬 타임존 가정에 영향받지 않는다.
function formatPace(distanceMeters, startTime, endTime) {
  var meters = Number(distanceMeters);
  if (!meters || !startTime || !endTime) return '';
  var seconds = (new Date(endTime) - new Date(startTime)) / 1000;
  if (!(seconds > 0)) return '';
  var paceSecPerKm = Math.round(seconds / (meters / 1000));
  var min = Math.floor(paceSecPerKm / 60);
  var sec = paceSecPerKm % 60;
  return min + ':' + (sec < 10 ? '0' : '') + sec + ' /km';
}

function isLoggedIn() {
  return !!localStorage.getItem(STORAGE_KEYS.accessToken);
}

function getMemberSno() {
  var v = localStorage.getItem(STORAGE_KEYS.memberSno);
  return v ? Number(v) : null;
}

// 서버 응답(TokenResponse)에는 memberSno가 없어서, access token(JWT)의 sub 클레임에서 꺼낸다.
// 서명 검증은 하지 않음 — 어차피 모든 API 호출은 서버가 매번 토큰을 재검증하므로,
// 여기서는 "내 글/댓글에만 삭제 버튼 보이기" 같은 화면 표시용으로만 쓴다.
function decodeJwtSubject(token) {
  try {
    var payload = token.split('.')[1];
    var json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
    return JSON.parse(json).sub;
  } catch (e) {
    return null;
  }
}

function storeAuth(tokenResponse) {
  localStorage.setItem(STORAGE_KEYS.accessToken, tokenResponse.accessToken);
  localStorage.setItem(STORAGE_KEYS.refreshToken, tokenResponse.refreshToken);
  localStorage.setItem(STORAGE_KEYS.nickname, tokenResponse.nickname || '');
  localStorage.setItem(STORAGE_KEYS.memberSno, decodeJwtSubject(tokenResponse.accessToken) || '');
}

function clearAuth() {
  localStorage.removeItem(STORAGE_KEYS.accessToken);
  localStorage.removeItem(STORAGE_KEYS.refreshToken);
  localStorage.removeItem(STORAGE_KEYS.nickname);
  localStorage.removeItem(STORAGE_KEYS.memberSno);
}

// 로그인 자체는 아직 토큰이 없으므로 apiFetch를 거치지 않는 별도 호출.
function login(memberId, password) {
  return fetch(API_BASE + '/api/member/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ memberId: memberId, password: password })
  }).then(function (res) {
    return res.json().then(function (json) {
      if (!res.ok || !json.success) {
        throw new Error((json && json.message) || '로그인에 실패했습니다.');
      }
      return json.data;
    });
  });
}

function logout() {
  var refreshToken = localStorage.getItem(STORAGE_KEYS.refreshToken);
  var done = refreshToken
    ? fetch(API_BASE + '/api/member/logout', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken: refreshToken })
      }).catch(function () {})
    : Promise.resolve();

  return done.then(function () {
    clearAuth();
    window.location.href = '/index.html';
  });
}

function reissueTokens() {
  var refreshToken = localStorage.getItem(STORAGE_KEYS.refreshToken);
  if (!refreshToken) {
    return Promise.reject(new Error('no refresh token'));
  }
  return fetch(API_BASE + '/api/member/reissue', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken: refreshToken })
  }).then(function (res) {
    return res.json().then(function (json) {
      if (!res.ok || !json.success) {
        throw new Error('reissue failed');
      }
      storeAuth(json.data);
      return json.data;
    });
  });
}

function rawFetch(path, options) {
  options = options || {};
  var headers = options.headers || {};
  var token = localStorage.getItem(STORAGE_KEYS.accessToken);
  if (token) {
    headers['Authorization'] = 'Bearer ' + token;
  }
  if (options.body) {
    headers['Content-Type'] = 'application/json';
  }
  return fetch(API_BASE + path, {
    method: options.method || 'GET',
    headers: headers,
    body: options.body ? JSON.stringify(options.body) : undefined
  });
}

// 공용 API 호출 래퍼. 게시판 조회(GET)는 비로그인도 호출 가능하고, 로그인 상태면 토큰을 자동 첨부한다.
// 로그인 상태에서 401이 오면(토큰 만료 등) reissue 후 한 번만 재시도.
function apiFetch(path, options) {
  return rawFetch(path, options).then(function (res) {
    if (res.status !== 401 || !isLoggedIn()) {
      return res.json();
    }
    return reissueTokens()
      .then(function () {
        return rawFetch(path, options).then(function (retryRes) {
          return retryRes.json();
        });
      })
      .catch(function () {
        clearAuth();
        window.location.href = '/login.html';
        return Promise.reject(new Error('unauthorized'));
      });
  });
}

// 게시글 작성/수정 에디터(contenteditable) 공통 로직 — index.html(작성 다이얼로그)과 post.html(수정 화면)이
// 같은 마크업 구조(에디터 div + 툴바 5개 버튼 + 숨긴 파일 input)를 공유하므로 여기서 한 번만 구현한다.
// opts: { editorEl, fileInputEl, boldBtn, italicBtn, linkBtn, imageBtn, videoBtn }
function createPostEditor(opts) {
  var mediaPlaceholderSeq = 0;
  var mediaPlaceholderPollTimers = {};

  function clearPollTimers() {
    Object.keys(mediaPlaceholderPollTimers).forEach(function (id) { clearInterval(mediaPlaceholderPollTimers[id]); });
    mediaPlaceholderPollTimers = {};
  }

  function setEmptyState() {
    opts.editorEl.classList.toggle('is-empty',
      opts.editorEl.textContent.trim() === '' && opts.editorEl.querySelectorAll('img, video').length === 0);
  }

  function isBlank() {
    return opts.editorEl.textContent.trim() === '' && opts.editorEl.querySelectorAll('img, video').length === 0;
  }

  function insertedMediaCount() {
    return opts.editorEl.querySelectorAll('img, video, .media-placeholder').length;
  }

  // 삽입 시점에 에디터 안에 유효한 선택 영역이 있으면 그 자리에, 없으면(다른 곳을 클릭해 포커스를 잃은 경우 등)
  // 에디터 맨 끝에 삽입한다. 비동기 업로드 중 Range가 무효해질 수 있는 미디어 삽입에서 특히 중요.
  function insertNodeAtCursor(buildNode) {
    var editor = opts.editorEl;
    editor.focus();
    var sel = window.getSelection();
    var range;
    if (sel.rangeCount > 0 && editor.contains(sel.getRangeAt(0).commonAncestorContainer)) {
      range = sel.getRangeAt(0);
    } else {
      range = document.createRange();
      range.selectNodeContents(editor);
      range.collapse(false);
    }
    var node = buildNode();
    range.deleteContents();
    range.insertNode(node);
    range.setStartAfter(node);
    range.collapse(true);
    sel.removeAllRanges();
    sel.addRange(range);
    return node;
  }

  function insertMediaPlaceholder(label) {
    var span = insertNodeAtCursor(function () {
      var el = document.createElement('span');
      el.id = 'media-placeholder-' + (++mediaPlaceholderSeq);
      el.className = 'media-placeholder';
      el.setAttribute('contenteditable', 'false');
      el.setAttribute('data-media-pending', 'true');
      el.textContent = label;
      return el;
    });
    setEmptyState();
    return span.id;
  }

  // execCommand('createLink')는 선택 영역이 collapsed(그냥 커서만 있는 상태)면 아무것도 삽입하지 않는
  // 브라우저 공통 동작이라, 그 경우엔 새 <a> 노드를 직접 만들어 커서 위치에 넣는다.
  function insertLink(url) {
    insertNodeAtCursor(function () {
      var a = document.createElement('a');
      a.href = url;
      a.textContent = url;
      return a;
    });
    setEmptyState();
  }

  function replacePlaceholderWithMedia(placeholderId, kind, url) {
    var span = document.getElementById(placeholderId);
    if (!span) return;
    var el = document.createElement(kind === 'image' ? 'img' : 'video');
    el.src = url;
    if (kind === 'video') el.controls = true;
    span.replaceWith(el);
    setEmptyState();
  }

  function markPlaceholderRejected(placeholderId, message) {
    var span = document.getElementById(placeholderId);
    if (!span) return;
    span.textContent = message;
    span.classList.add('rejected');
    span.removeAttribute('data-media-pending');
    setTimeout(function () {
      if (span.parentNode) span.remove();
      setEmptyState();
    }, 2000);
  }

  function hasPendingPlaceholders() {
    return opts.editorEl.querySelector('[data-media-pending="true"]') !== null;
  }

  // 이미지는 /complete가 동기 모더레이션 결과(APPROVED/REJECTED)를 바로 주지만, 동영상은 PENDING을 주고
  // 비동기 작업만 시작되므로 그 경우에만 /status를 주기적으로 폴링해서 최종 결과를 받는다.
  function applyMediaStatus(placeholderId, kind, statusData) {
    if (statusData.status === 'APPROVED') {
      replacePlaceholderWithMedia(placeholderId, kind, statusData.url);
    } else if (statusData.status === 'REJECTED') {
      markPlaceholderRejected(placeholderId, t('attachMediaRejected'));
    } else {
      var span = document.getElementById(placeholderId);
      if (span) span.textContent = t('attachMediaModerating');
      mediaPlaceholderPollTimers[placeholderId] = setInterval(function () {
        apiFetch('/api/media/' + statusData.mediaSno + '/status').then(function (json) {
          if (!json.success || json.data.status === 'PENDING') return;
          clearInterval(mediaPlaceholderPollTimers[placeholderId]);
          delete mediaPlaceholderPollTimers[placeholderId];
          applyMediaStatus(placeholderId, kind, json.data);
        });
      }, 3000);
    }
  }

  function addFileAttachment(file) {
    if (insertedMediaCount() >= 10) {
      alert(t('attachMediaMaxReached'));
      return;
    }
    var kind = file.type.indexOf('image/') === 0 ? 'image' : (file.type.indexOf('video/') === 0 ? 'video' : null);
    if (!kind) {
      alert(t('attachMediaInvalidType'));
      return;
    }
    var maxBytes = kind === 'image' ? 10 * 1024 * 1024 : 200 * 1024 * 1024;
    if (file.size > maxBytes) {
      alert(t('attachMediaTooLarge'));
      return;
    }

    var placeholderId = insertMediaPlaceholder(t('attachMediaUploading'));
    var mediaSno = null;

    apiFetch('/api/media/uploads', {
      method: 'POST',
      body: { type: kind.toUpperCase(), mimeType: file.type, fileSizeBytes: file.size }
    }).then(function (json) {
      if (!json.success) { throw new Error(json.message || 'upload init failed'); }
      mediaSno = json.data.mediaSno;
      // S3가 대상이라 apiFetch(API_BASE로 감)가 아닌 순수 fetch로 presigned URL에 직접 PUT.
      return fetch(json.data.uploadUrl, { method: 'PUT', headers: { 'Content-Type': file.type }, body: file });
    }).then(function (putRes) {
      if (!putRes.ok) { throw new Error('s3 put failed'); }
      var span = document.getElementById(placeholderId);
      if (span) span.textContent = t('attachMediaModerating');
      return apiFetch('/api/media/' + mediaSno + '/complete', { method: 'POST' });
    }).then(function (json) {
      if (!json.success) { throw new Error(json.message || 'complete failed'); }
      applyMediaStatus(placeholderId, kind, json.data);
    }).catch(function () {
      markPlaceholderRejected(placeholderId, t('attachMediaUploadFailed'));
    });
  }

  // 툴바 버튼은 에디터 밖에 있어 클릭하면 선택 영역이 풀릴 수 있으므로, mousedown에서
  // preventDefault로 포커스 이동 자체를 막아 클릭 시점에도 커서 위치가 그대로 유지되게 한다.
  [opts.boldBtn, opts.italicBtn, opts.linkBtn, opts.imageBtn, opts.videoBtn].forEach(function (btn) {
    btn.addEventListener('mousedown', function (e) { e.preventDefault(); });
  });
  opts.boldBtn.addEventListener('click', function () { document.execCommand('bold'); });
  opts.italicBtn.addEventListener('click', function () { document.execCommand('italic'); });
  opts.linkBtn.addEventListener('click', function () {
    var url = prompt(t('editorLinkPrompt'));
    if (!url) return;
    if (!/^https?:\/\//i.test(url)) { alert(t('editorLinkInvalid')); return; }
    var sel = window.getSelection();
    var hasSelection = sel.rangeCount > 0 && !sel.getRangeAt(0).collapsed &&
      opts.editorEl.contains(sel.getRangeAt(0).commonAncestorContainer);
    if (hasSelection) {
      document.execCommand('createLink', false, url);
    } else {
      insertLink(url);
    }
  });
  opts.imageBtn.addEventListener('click', function () {
    opts.fileInputEl.accept = 'image/jpeg,image/png,image/webp,image/gif';
    opts.fileInputEl.click();
  });
  opts.videoBtn.addEventListener('click', function () {
    opts.fileInputEl.accept = 'video/mp4,video/quicktime';
    opts.fileInputEl.click();
  });
  opts.fileInputEl.addEventListener('change', function (e) {
    var file = e.target.files[0];
    if (file) addFileAttachment(file);
    e.target.value = '';
  });
  opts.editorEl.addEventListener('input', setEmptyState);

  return {
    reset: function (html) {
      opts.editorEl.innerHTML = html || '';
      setEmptyState();
      clearPollTimers();
    },
    getContentHtml: function () { return opts.editorEl.innerHTML.trim(); },
    isBlank: isBlank,
    hasPending: hasPendingPlaceholders,
    clearPollTimers: clearPollTimers
  };
}

// 게시글 작성/수정 화면의 "내 러닝기록 첨부" select를 채운다 — index.html(작성)/post.html(수정)이 공유.
// extraRecord: 수정 화면에서 기존에 첨부돼 있던 기록이 최근 30일 밖으로 밀려나 목록에 없을 수 있어,
// PostDetailResponse.attachedRecord를 그대로 넘겨 목록에 없으면 맨 앞에 끼워 넣기 위한 용도(선택).
function loadRecordOptions(selectEl, selectedRecordSno, extraRecord) {
  selectEl.innerHTML = '<option value="">' + t('attachRecordNone') + '</option>';
  return apiFetch('/api/record/recent-30-days').then(function (json) {
    var records = (json && json.data) || [];
    if (extraRecord && !records.some(function (r) { return r.recordSno === extraRecord.recordSno; })) {
      records = [extraRecord].concat(records);
    }
    if (records.length === 0) {
      selectEl.innerHTML += '<option value="" disabled>' + t('attachRecordEmpty') + '</option>';
      return;
    }
    records.forEach(function (r) {
      var label = formatRecordDateTime(r.startTime) + ' · ' + formatDistanceKm(r.distance);
      var selected = selectedRecordSno && Number(selectedRecordSno) === r.recordSno ? ' selected' : '';
      selectEl.innerHTML += '<option value="' + r.recordSno + '"' + selected + '>' + escapeHtml(label) + '</option>';
    });
  });
}

function requireLogin() {
  if (!isLoggedIn()) {
    window.location.href = '/login.html';
  }
}

// 글쓰기/댓글/투표처럼 로그인이 필요한 동작을 시작하기 전에 호출. 비로그인이면 로그인 페이지로 보내고 true를 반환.
function redirectToLoginIfNeeded() {
  if (!isLoggedIn()) {
    window.location.href = '/login.html';
    return true;
  }
  return false;
}
