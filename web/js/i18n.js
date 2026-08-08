// PaceLeague UI 다국어 사전 — index.html/login.html/post.html이 app.js 다음에 로드해서 공유.
// 게시글/댓글 원문 번역(AWS Translate 호출)과는 별개다: 이건 버튼/라벨 같은 정적 UI 문자열만 다룬다.

var SUPPORTED_LANGUAGES = [
  { code: 'ko', flag: '🇰🇷', name: '한국어' },
  { code: 'en', flag: '🇺🇸', name: 'English' },
  { code: 'ja', flag: '🇯🇵', name: '日本語' },
  { code: 'zh', flag: '🇨🇳', name: '中文' },
  { code: 'es', flag: '🇪🇸', name: 'Español' },
  { code: 'fr', flag: '🇫🇷', name: 'Français' },
  { code: 'de', flag: '🇩🇪', name: 'Deutsch' },
  { code: 'pt', flag: '🇵🇹', name: 'Português' },
  { code: 'vi', flag: '🇻🇳', name: 'Tiếng Việt' },
  { code: 'th', flag: '🇹🇭', name: 'ไทย' }
];

var TRANSLATIONS = {
  ko: {
    login: '로그인', write: '글쓰기', logout: '로그아웃', sortNew: '최신순', sortTop: '인기순',
    titlePlaceholder: '제목', contentPlaceholder: '내용을 입력하세요', cancel: '취소', submit: '등록',
    noPosts: '아직 게시글이 없습니다.', noBoards: '보드가 없습니다.', prevPage: '이전', nextPage: '다음',
    writeErrorEmpty: '제목과 내용을 모두 입력해주세요.', writeFailed: '작성에 실패했습니다.',
    reactionLabel: '추천', commentCountLabel: '댓글', viewCountLabel: '조회',
    loginHeading: 'Pace League 로그인', memberIdPlaceholder: '아이디', passwordPlaceholder: '비밀번호',
    backToMain: '← 메인으로', loginFailed: '로그인에 실패했습니다.',
    backToList: '← 목록', mainLink: '메인', voteUp: '▲ 추천', voteDown: '▼ 비추천', delete: '삭제',
    confirmDeletePost: '게시글을 삭제할까요? 댓글도 함께 삭제됩니다.', confirmDeleteComment: '댓글을 삭제할까요?',
    commentsTitle: '댓글', commentPlaceholder: '댓글을 입력하세요', commentSubmit: '댓글 작성',
    noComments: '아직 댓글이 없습니다.', replyLink: '답글', replyPlaceholder: '답글을 입력하세요',
    translateShow: '번역 보기', translateHide: '원문 보기', translateFailed: '번역에 실패했습니다.'
  },
  en: {
    login: 'Login', write: 'Write', logout: 'Logout', sortNew: 'New', sortTop: 'Top',
    titlePlaceholder: 'Title', contentPlaceholder: 'Write your content', cancel: 'Cancel', submit: 'Post',
    noPosts: 'No posts yet.', noBoards: 'No boards available.', prevPage: 'Prev', nextPage: 'Next',
    writeErrorEmpty: 'Please fill in both title and content.', writeFailed: 'Failed to post.',
    reactionLabel: 'Votes', commentCountLabel: 'Comments', viewCountLabel: 'Views',
    loginHeading: 'Log in to Pace League', memberIdPlaceholder: 'Username', passwordPlaceholder: 'Password',
    backToMain: '← Back to home', loginFailed: 'Login failed.',
    backToList: '← Back', mainLink: 'Home', voteUp: '▲ Upvote', voteDown: '▼ Downvote', delete: 'Delete',
    confirmDeletePost: 'Delete this post? Comments will be deleted too.', confirmDeleteComment: 'Delete this comment?',
    commentsTitle: 'Comments', commentPlaceholder: 'Write a comment', commentSubmit: 'Comment',
    noComments: 'No comments yet.', replyLink: 'Reply', replyPlaceholder: 'Write a reply',
    translateShow: 'Show translation', translateHide: 'Show original', translateFailed: 'Translation failed.'
  },
  ja: {
    login: 'ログイン', write: '投稿する', logout: 'ログアウト', sortNew: '新着順', sortTop: '人気順',
    titlePlaceholder: 'タイトル', contentPlaceholder: '内容を入力してください', cancel: 'キャンセル', submit: '投稿',
    noPosts: 'まだ投稿がありません。', noBoards: '掲示板がありません。', prevPage: '前へ', nextPage: '次へ',
    writeErrorEmpty: 'タイトルと内容を入力してください。', writeFailed: '投稿に失敗しました。',
    reactionLabel: '評価', commentCountLabel: 'コメント', viewCountLabel: '閲覧',
    loginHeading: 'Pace League ログイン', memberIdPlaceholder: 'アイディー', passwordPlaceholder: 'パスワード',
    backToMain: '← ホームへ', loginFailed: 'ログインに失敗しました。',
    backToList: '← 一覧へ', mainLink: 'ホーム', voteUp: '▲ 高評価', voteDown: '▼ 低評価', delete: '削除',
    confirmDeletePost: '投稿を削除しますか？コメントも削除されます。', confirmDeleteComment: 'コメントを削除しますか？',
    commentsTitle: 'コメント', commentPlaceholder: 'コメントを入力してください', commentSubmit: 'コメントする',
    noComments: 'まだコメントがありません。', replyLink: '返信', replyPlaceholder: '返信を入力してください',
    translateShow: '翻訳を見る', translateHide: '原文を見る', translateFailed: '翻訳に失敗しました。'
  },
  zh: {
    login: '登录', write: '写帖子', logout: '登出', sortNew: '最新', sortTop: '热门',
    titlePlaceholder: '标题', contentPlaceholder: '请输入内容', cancel: '取消', submit: '发布',
    noPosts: '暂无帖子。', noBoards: '暂无版块。', prevPage: '上一页', nextPage: '下一页',
    writeErrorEmpty: '请填写标题和内容。', writeFailed: '发布失败。',
    reactionLabel: '点赞', commentCountLabel: '评论', viewCountLabel: '浏览',
    loginHeading: '登录 Pace League', memberIdPlaceholder: '账号', passwordPlaceholder: '密码',
    backToMain: '← 返回首页', loginFailed: '登录失败。',
    backToList: '← 返回列表', mainLink: '首页', voteUp: '▲ 赞', voteDown: '▼ 踩', delete: '删除',
    confirmDeletePost: '确定要删除这篇帖子吗？评论也会一并删除。', confirmDeleteComment: '确定要删除这条评论吗？',
    commentsTitle: '评论', commentPlaceholder: '请输入评论', commentSubmit: '发表评论',
    noComments: '暂无评论。', replyLink: '回复', replyPlaceholder: '请输入回复',
    translateShow: '查看翻译', translateHide: '查看原文', translateFailed: '翻译失败。'
  },
  es: {
    login: 'Iniciar sesión', write: 'Publicar', logout: 'Cerrar sesión', sortNew: 'Recientes', sortTop: 'Popular',
    titlePlaceholder: 'Título', contentPlaceholder: 'Escribe el contenido', cancel: 'Cancelar', submit: 'Publicar',
    noPosts: 'Aún no hay publicaciones.', noBoards: 'No hay foros disponibles.', prevPage: 'Anterior', nextPage: 'Siguiente',
    writeErrorEmpty: 'Completa el título y el contenido.', writeFailed: 'Error al publicar.',
    reactionLabel: 'Votos', commentCountLabel: 'Comentarios', viewCountLabel: 'Vistas',
    loginHeading: 'Inicia sesión en Pace League', memberIdPlaceholder: 'Usuario', passwordPlaceholder: 'Contraseña',
    backToMain: '← Volver al inicio', loginFailed: 'Error al iniciar sesión.',
    backToList: '← Volver', mainLink: 'Inicio', voteUp: '▲ Voto positivo', voteDown: '▼ Voto negativo', delete: 'Eliminar',
    confirmDeletePost: '¿Eliminar esta publicación? Los comentarios también se eliminarán.', confirmDeleteComment: '¿Eliminar este comentario?',
    commentsTitle: 'Comentarios', commentPlaceholder: 'Escribe un comentario', commentSubmit: 'Comentar',
    noComments: 'Aún no hay comentarios.', replyLink: 'Responder', replyPlaceholder: 'Escribe una respuesta',
    translateShow: 'Ver traducción', translateHide: 'Ver original', translateFailed: 'Error al traducir.'
  },
  fr: {
    login: 'Connexion', write: 'Publier', logout: 'Déconnexion', sortNew: 'Récent', sortTop: 'Populaire',
    titlePlaceholder: 'Titre', contentPlaceholder: 'Écrivez le contenu', cancel: 'Annuler', submit: 'Publier',
    noPosts: 'Aucune publication pour le moment.', noBoards: 'Aucun forum disponible.', prevPage: 'Précédent', nextPage: 'Suivant',
    writeErrorEmpty: 'Veuillez remplir le titre et le contenu.', writeFailed: 'Échec de la publication.',
    reactionLabel: 'Votes', commentCountLabel: 'Commentaires', viewCountLabel: 'Vues',
    loginHeading: 'Connexion à Pace League', memberIdPlaceholder: 'Identifiant', passwordPlaceholder: 'Mot de passe',
    backToMain: "← Retour à l'accueil", loginFailed: 'Échec de la connexion.',
    backToList: '← Retour', mainLink: 'Accueil', voteUp: '▲ Voter pour', voteDown: '▼ Voter contre', delete: 'Supprimer',
    confirmDeletePost: 'Supprimer cette publication ? Les commentaires seront aussi supprimés.', confirmDeleteComment: 'Supprimer ce commentaire ?',
    commentsTitle: 'Commentaires', commentPlaceholder: 'Écrivez un commentaire', commentSubmit: 'Commenter',
    noComments: 'Aucun commentaire pour le moment.', replyLink: 'Répondre', replyPlaceholder: 'Écrivez une réponse',
    translateShow: 'Voir la traduction', translateHide: "Voir l'original", translateFailed: 'Échec de la traduction.'
  },
  de: {
    login: 'Anmelden', write: 'Beitrag erstellen', logout: 'Abmelden', sortNew: 'Neu', sortTop: 'Beliebt',
    titlePlaceholder: 'Titel', contentPlaceholder: 'Inhalt eingeben', cancel: 'Abbrechen', submit: 'Veröffentlichen',
    noPosts: 'Noch keine Beiträge.', noBoards: 'Keine Foren verfügbar.', prevPage: 'Zurück', nextPage: 'Weiter',
    writeErrorEmpty: 'Bitte Titel und Inhalt ausfüllen.', writeFailed: 'Veröffentlichen fehlgeschlagen.',
    reactionLabel: 'Stimmen', commentCountLabel: 'Kommentare', viewCountLabel: 'Aufrufe',
    loginHeading: 'Bei Pace League anmelden', memberIdPlaceholder: 'Benutzername', passwordPlaceholder: 'Passwort',
    backToMain: '← Zur Startseite', loginFailed: 'Anmeldung fehlgeschlagen.',
    backToList: '← Zurück', mainLink: 'Start', voteUp: '▲ Positiv bewerten', voteDown: '▼ Negativ bewerten', delete: 'Löschen',
    confirmDeletePost: 'Diesen Beitrag löschen? Kommentare werden ebenfalls gelöscht.', confirmDeleteComment: 'Diesen Kommentar löschen?',
    commentsTitle: 'Kommentare', commentPlaceholder: 'Kommentar schreiben', commentSubmit: 'Kommentieren',
    noComments: 'Noch keine Kommentare.', replyLink: 'Antworten', replyPlaceholder: 'Antwort schreiben',
    translateShow: 'Übersetzung anzeigen', translateHide: 'Original anzeigen', translateFailed: 'Übersetzung fehlgeschlagen.'
  },
  pt: {
    login: 'Entrar', write: 'Publicar', logout: 'Sair', sortNew: 'Recentes', sortTop: 'Populares',
    titlePlaceholder: 'Título', contentPlaceholder: 'Escreva o conteúdo', cancel: 'Cancelar', submit: 'Publicar',
    noPosts: 'Ainda não há publicações.', noBoards: 'Nenhum fórum disponível.', prevPage: 'Anterior', nextPage: 'Próximo',
    writeErrorEmpty: 'Preencha o título e o conteúdo.', writeFailed: 'Falha ao publicar.',
    reactionLabel: 'Votos', commentCountLabel: 'Comentários', viewCountLabel: 'Visualizações',
    loginHeading: 'Entrar no Pace League', memberIdPlaceholder: 'Usuário', passwordPlaceholder: 'Senha',
    backToMain: '← Voltar ao início', loginFailed: 'Falha ao entrar.',
    backToList: '← Voltar', mainLink: 'Início', voteUp: '▲ Curtir', voteDown: '▼ Não curtir', delete: 'Excluir',
    confirmDeletePost: 'Excluir esta publicação? Os comentários também serão excluídos.', confirmDeleteComment: 'Excluir este comentário?',
    commentsTitle: 'Comentários', commentPlaceholder: 'Escreva um comentário', commentSubmit: 'Comentar',
    noComments: 'Ainda não há comentários.', replyLink: 'Responder', replyPlaceholder: 'Escreva uma resposta',
    translateShow: 'Ver tradução', translateHide: 'Ver original', translateFailed: 'Falha na tradução.'
  },
  vi: {
    login: 'Đăng nhập', write: 'Viết bài', logout: 'Đăng xuất', sortNew: 'Mới nhất', sortTop: 'Nổi bật',
    titlePlaceholder: 'Tiêu đề', contentPlaceholder: 'Nhập nội dung', cancel: 'Hủy', submit: 'Đăng',
    noPosts: 'Chưa có bài viết nào.', noBoards: 'Không có bảng nào.', prevPage: 'Trước', nextPage: 'Sau',
    writeErrorEmpty: 'Vui lòng nhập đầy đủ tiêu đề và nội dung.', writeFailed: 'Đăng bài thất bại.',
    reactionLabel: 'Lượt bình chọn', commentCountLabel: 'Bình luận', viewCountLabel: 'Lượt xem',
    loginHeading: 'Đăng nhập Pace League', memberIdPlaceholder: 'Tên đăng nhập', passwordPlaceholder: 'Mật khẩu',
    backToMain: '← Về trang chủ', loginFailed: 'Đăng nhập thất bại.',
    backToList: '← Danh sách', mainLink: 'Trang chủ', voteUp: '▲ Thích', voteDown: '▼ Không thích', delete: 'Xóa',
    confirmDeletePost: 'Xóa bài viết này? Các bình luận cũng sẽ bị xóa.', confirmDeleteComment: 'Xóa bình luận này?',
    commentsTitle: 'Bình luận', commentPlaceholder: 'Nhập bình luận', commentSubmit: 'Bình luận',
    noComments: 'Chưa có bình luận nào.', replyLink: 'Trả lời', replyPlaceholder: 'Nhập trả lời',
    translateShow: 'Xem bản dịch', translateHide: 'Xem nguyên văn', translateFailed: 'Dịch thất bại.'
  },
  th: {
    login: 'เข้าสู่ระบบ', write: 'เขียนโพสต์', logout: 'ออกจากระบบ', sortNew: 'ใหม่ล่าสุด', sortTop: 'ยอดนิยม',
    titlePlaceholder: 'หัวข้อ', contentPlaceholder: 'กรอกเนื้อหา', cancel: 'ยกเลิก', submit: 'โพสต์',
    noPosts: 'ยังไม่มีโพสต์', noBoards: 'ไม่มีบอร์ด', prevPage: 'ก่อนหน้า', nextPage: 'ถัดไป',
    writeErrorEmpty: 'กรุณากรอกหัวข้อและเนื้อหาให้ครบ', writeFailed: 'โพสต์ไม่สำเร็จ',
    reactionLabel: 'โหวต', commentCountLabel: 'ความคิดเห็น', viewCountLabel: 'เข้าชม',
    loginHeading: 'เข้าสู่ระบบ Pace League', memberIdPlaceholder: 'ชื่อผู้ใช้', passwordPlaceholder: 'รหัสผ่าน',
    backToMain: '← กลับหน้าแรก', loginFailed: 'เข้าสู่ระบบไม่สำเร็จ',
    backToList: '← กลับไปที่รายการ', mainLink: 'หน้าแรก', voteUp: '▲ ถูกใจ', voteDown: '▼ ไม่ถูกใจ', delete: 'ลบ',
    confirmDeletePost: 'ลบโพสต์นี้หรือไม่? ความคิดเห็นจะถูกลบไปด้วย', confirmDeleteComment: 'ลบความคิดเห็นนี้หรือไม่?',
    commentsTitle: 'ความคิดเห็น', commentPlaceholder: 'แสดงความคิดเห็น', commentSubmit: 'แสดงความคิดเห็น',
    noComments: 'ยังไม่มีความคิดเห็น', replyLink: 'ตอบกลับ', replyPlaceholder: 'พิมพ์การตอบกลับ',
    translateShow: 'ดูคำแปล', translateHide: 'ดูต้นฉบับ', translateFailed: 'แปลไม่สำเร็จ'
  }
};

function getLang() {
  var stored = localStorage.getItem('pl_lang');
  if (stored && TRANSLATIONS[stored]) return stored;
  var nav = ((navigator.language || 'ko').split('-')[0]).toLowerCase();
  return TRANSLATIONS[nav] ? nav : 'ko';
}

function setLang(code) {
  localStorage.setItem('pl_lang', code);
}

function t(key) {
  var lang = getLang();
  return (TRANSLATIONS[lang] && TRANSLATIONS[lang][key]) || TRANSLATIONS.ko[key] || key;
}

function renderLangSelect(selectEl) {
  selectEl.innerHTML = SUPPORTED_LANGUAGES.map(function (l) {
    return '<option value="' + l.code + '">' + l.flag + ' ' + l.name + '</option>';
  }).join('');
  selectEl.value = getLang();
  selectEl.addEventListener('change', function () {
    setLang(selectEl.value);
    window.location.reload();
  });
}
