// PaceLeague 관리자 페이지 공용 스크립트 — web/admin/**에서만 쓰인다.
// member 로그인(JWT + localStorage pl_access_token)과 완전히 별개의 세션 체계다:
// 관리자는 Redis 서버 세션(X-Admin-Session 헤더)이라 로그아웃 시 서버에서 즉시 무효화할 수 있다.

var ADMIN_API_BASE = (location.hostname === 'localhost' || location.hostname === '127.0.0.1')
  ? 'http://localhost:8080'
  : 'https://api.paceleague.co.kr';

var ADMIN_SESSION_KEY = 'pl_admin_session';
var ADMIN_SESSION_HEADER = 'X-Admin-Session';

var ADMIN_MENU = [
  { key: 'members', label: '회원관리', href: '/admin/members.html' },
  { key: 'ranking', label: '랭킹관리', href: '/admin/ranking.html' },
  { key: 'records', label: '러닝데이터관리', href: '/admin/records.html' },
  { key: 'territory', label: '랜드잇데이터관리', href: '/admin/territory.html' },
  { key: 'settings', label: '설정', href: '/admin/settings.html' }
];

function getAdminSession() {
  return localStorage.getItem(ADMIN_SESSION_KEY);
}

function setAdminSession(token) {
  localStorage.setItem(ADMIN_SESSION_KEY, token);
}

function clearAdminSession() {
  localStorage.removeItem(ADMIN_SESSION_KEY);
}

// member의 apiFetch와 달리 401이어도 자동 재시도(reissue)하지 않는다 — 관리자 세션은 만료되면
// 그냥 다시 로그인해야 하는 서버 세션이라 재발급 개념이 없다.
function adminApiFetch(path, options) {
  options = options || {};
  var headers = options.headers || {};
  var token = getAdminSession();
  if (token) headers[ADMIN_SESSION_HEADER] = token;
  if (options.body) headers['Content-Type'] = 'application/json';

  return fetch(ADMIN_API_BASE + path, {
    method: options.method || 'GET',
    headers: headers,
    body: options.body ? JSON.stringify(options.body) : undefined
  }).then(function (res) {
    return res.json().then(function (json) {
      return { ok: res.ok, status: res.status, json: json };
    });
  });
}

function adminLogin(adminId, password) {
  return fetch(ADMIN_API_BASE + '/api/admin/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ adminId: adminId, password: password })
  }).then(function (res) {
    return res.json().then(function (json) {
      if (!res.ok || !json.success) {
        throw new Error((json && json.message) || '로그인에 실패했습니다.');
      }
      return json.data;
    });
  });
}

function adminLogout() {
  return adminApiFetch('/api/admin/logout', { method: 'POST' })
    .catch(function () {})
    .then(function () {
      clearAdminSession();
      window.location.href = '/admin/';
    });
}

// 관리자 페이지마다 최상단에서 호출. 세션이 없거나 서버에서 무효(401)로 판정하면 즉시 로그인 화면으로
// 돌려보내고, 유효할 때만 onSuccess가 실행되어 화면 내용을 보여준다 — "세션 없으면 안쪽 페이지가
// 전부 안 보여야 한다"는 요구사항을 각 페이지가 콘텐츠를 hidden 상태로 시작해서 만족시킨다.
function requireAdminSession(onSuccess) {
  if (!getAdminSession()) {
    window.location.href = '/admin/';
    return;
  }
  adminApiFetch('/api/admin/me').then(function (result) {
    if (!result.ok || !result.json.success) {
      throw new Error('unauthorized');
    }
    onSuccess(result.json.data);
  }).catch(function () {
    clearAdminSession();
    window.location.href = '/admin/';
  });
}

function renderAdminHeader() {
  var el = document.getElementById('admin-header');
  if (!el) return;
  el.innerHTML =
    '<div class="topbar"><div class="topbar-inner" style="justify-content: space-between;">' +
      '<a class="brand" href="/admin/members.html">' +
        '<img class="brand-mark" src="/img/favicon.png" alt="PACELEAGUE">' +
        '<span class="brand-name">PACELEAGUE ADMIN</span>' +
      '</a>' +
      '<a class="btn ghost" href="#" id="admin-logout-link">로그아웃</a>' +
    '</div></div>';
  document.getElementById('admin-logout-link').addEventListener('click', function (e) {
    e.preventDefault();
    adminLogout();
  });
}

function renderAdminSidebar(activeKey) {
  var el = document.getElementById('admin-sidebar');
  if (!el) return;
  el.innerHTML = ADMIN_MENU.map(function (m) {
    var active = m.key === activeKey ? ' active' : '';
    return '<a class="admin-menu-link' + active + '" href="' + m.href + '">' + m.label + '</a>';
  }).join('');
}

function renderAdminLayout(activeKey) {
  renderAdminHeader();
  renderAdminSidebar(activeKey);
}
