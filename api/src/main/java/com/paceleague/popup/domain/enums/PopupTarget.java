package com.paceleague.popup.domain.enums;

// 팝업을 어디에 띄울지 — 공개 조회 API는 파라미터 없이 노출 대상 전부를 내려주고, 클라이언트가 이 값으로 거른다
// (웹은 WEB/BOTH, 앱은 APP/BOTH).
public enum PopupTarget {
    WEB,
    APP,
    BOTH
}
