// PaceLeague 커뮤니티 공유 스크립트 — login.html/board.html/post.html/index.html에서 공통으로 사용.

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

function isLoggedIn() {
  return !!localStorage.getItem(STORAGE_KEYS.accessToken);
}

function getNickname() {
  return localStorage.getItem(STORAGE_KEYS.nickname) || '';
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
  headers['Authorization'] = 'Bearer ' + localStorage.getItem(STORAGE_KEYS.accessToken);
  if (options.body) {
    headers['Content-Type'] = 'application/json';
  }
  return fetch(API_BASE + path, {
    method: options.method || 'GET',
    headers: headers,
    body: options.body ? JSON.stringify(options.body) : undefined
  });
}

// 인증이 필요한 API 호출 공용 래퍼. 401이면 reissue 후 한 번만 재시도.
function apiFetch(path, options) {
  return rawFetch(path, options).then(function (res) {
    if (res.status !== 401) {
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

function requireLogin() {
  if (!isLoggedIn()) {
    window.location.href = '/login.html';
  }
}
