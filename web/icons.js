/*
 * Bộ icon VinaSpends "Vintage 70s" – vẽ tay bằng SVG, không dùng thư viện icon nào.
 * Lưới 24×24, nét 2px bo tròn, tô nhiều màu (bảng màu P, map C + chi tiết X ở cuối file). Lớp:
 *   f  = tô nhạt cùng màu nét (duotone kiểu in lụa) + giữ nét viền
 *   d  = chỉ tô nhạt, không viền
 *   s  = tô đặc cùng màu nét (chấm, điểm nhấn)
 * Dùng: VinaIcons.svg(name) -> chuỗi <svg>; VinaIcons.has(name); VinaIcons.GROUPS cho bộ chọn icon.
 */
(function (root) {
  "use strict";
  var I = {};

  // ---------- Giao diện ----------
  I["home"] = '<path class="d" d="M5.5 10.2 12 4.6l6.5 5.6V20h-13z"/><path d="M3.5 11.4 12 4l8.5 7.4"/><path d="M5.5 9.8V20h13V9.8"/><path d="M10 20v-5.2a2 2 0 0 1 4 0V20"/>';
  I["chart"] = '<path d="M3.5 20.5h17"/><rect class="f" x="5" y="11" width="3.6" height="9.5" rx="1.2"/><rect x="10.2" y="5" width="3.6" height="15.5" rx="1.2"/><rect class="f" x="15.4" y="13.5" width="3.6" height="7" rx="1.2"/>';
  I["pie"] = '<path class="f" d="M12 3.5v8.5h8.5A8.5 8.5 0 0 0 12 3.5z"/><path d="M10 5.7A8.5 8.5 0 1 0 18.3 14"/>';
  I["trend-up"] = '<path d="M3.5 17.5 9 12l3.5 3.5 8-8"/><path d="M15 7.5h5.5V13"/><path class="d" d="M3.5 20.5h17v-3l-8 0-3.5-3.5-5.5 5.5z"/>';
  I["trend-down"] = '<path d="M3.5 7 9 12.5l3.5-3.5 8 8"/><path d="M15 17h5.5v-5.5"/>';
  I["tags"] = '<path class="f" d="M3.5 4.8V11l8.3 8.3a1.6 1.6 0 0 0 2.3 0l5.3-5.3a1.6 1.6 0 0 0 0-2.3L11.1 3.5H4.8a1.3 1.3 0 0 0-1.3 1.3z"/><circle class="s" cx="7.6" cy="7.6" r="1.4"/>';
  I["tag"] = I["tags"];
  I["wallet"] = '<path class="f" d="M4 7.5h15a1.5 1.5 0 0 1 1.5 1.5v9.5A1.5 1.5 0 0 1 19 20H5.5A2 2 0 0 1 3.5 18V6.5"/><path d="M3.5 6.5A2 2 0 0 1 5.5 4.5H16v3"/><path d="M20.5 11.5h-4a2 2 0 0 0 0 4h4"/><circle class="s" cx="16.6" cy="13.5" r="1"/>';
  I["plus"] = '<path d="M12 5v14M5 12h14"/>';
  I["minus"] = '<path d="M5 12h14"/>';
  I["x"] = '<path d="M6 6l12 12M18 6 6 18"/>';
  I["check"] = '<path d="m4.5 12.5 5 5 10-11"/>';
  I["chevron-left"] = '<path d="m14.5 5-7 7 7 7"/>';
  I["chevron-right"] = '<path d="m9.5 5 7 7-7 7"/>';
  I["chevron-up"] = '<path d="m5 14.5 7-7 7 7"/>';
  I["chevron-down"] = '<path d="m5 9.5 7 7 7-7"/>';
  I["arrow-left"] = '<path d="M19 12H5m6-6-6 6 6 6"/>';
  I["arrow-up-right"] = '<path d="M5.5 18.5 18.5 5.5M8 5.5h10.5V16"/>';
  I["arrow-down-left"] = '<path d="M18.5 5.5 5.5 18.5M16 18.5H5.5V8"/>';
  I["swap"] = '<path d="M4 8.5h14l-3.5-3.5M20 15.5H6l3.5 3.5"/>';
  I["settings"] = '<path class="f" d="M10.3 3.5h3.4l.6 2.5 1.6.9 2.4-.8 1.7 3-1.9 1.7v1.9l1.9 1.7-1.7 3-2.4-.8-1.6.9-.6 2.5h-3.4l-.6-2.5-1.6-.9-2.4.8-1.7-3 1.9-1.7v-1.9L4 9.1l1.7-3 2.4.8 1.6-.9z"/><circle cx="12" cy="12" r="2.8"/>';
  I["palette"] = '<path class="f" d="M12 3.5a8.5 8.5 0 0 0 0 17c1.3 0 1.9-.9 1.6-2-.3-1.3.5-2.5 1.9-2.5h2a3 3 0 0 0 3-3 8.5 8.5 0 0 0-8.5-9.5z"/><circle class="s" cx="7.8" cy="11" r="1.3"/><circle class="s" cx="10" cy="7.3" r="1.3"/><circle class="s" cx="14.5" cy="7.6" r="1.3"/>';
  I["sun"] = '<circle class="f" cx="12" cy="12" r="4"/><path d="M12 2.5v2.2M12 19.3v2.2M2.5 12h2.2M19.3 12h2.2M5.3 5.3l1.6 1.6M17.1 17.1l1.6 1.6M5.3 18.7l1.6-1.6M17.1 6.9l1.6-1.6"/>';
  I["moon"] = '<path class="f" d="M19.5 14.5A8 8 0 0 1 9.5 4.5a8 8 0 1 0 10 10z"/>';
  I["cloud"] = '<path class="f" d="M7 18.5h10.5a4 4 0 0 0 .6-8A6 6 0 0 0 6.5 9.6 4.5 4.5 0 0 0 7 18.5z"/>';
  I["cloud-off"] = '<path d="M7 18.5h10.5M20.6 16.3a4 4 0 0 0-2.5-5.8A6 6 0 0 0 9.4 6.3M6.6 9.6A4.5 4.5 0 0 0 7 18.5"/><path d="M3.5 3.5l17 17"/>';
  I["cloud-up"] = '<path class="f" d="M7 18.5h10.5a4 4 0 0 0 .6-8A6 6 0 0 0 6.5 9.6 4.5 4.5 0 0 0 7 18.5z"/><path d="M12 16v-5.5m-2.5 2.5L12 10.5l2.5 2.5"/>';
  I["refresh"] = '<path d="M19.5 9A8 8 0 0 0 5 7.5M4.5 15A8 8 0 0 0 19 16.5"/><path d="M19.8 4v5h-5M4.2 20v-5h5"/>';
  I["zap"] = '<path class="f" d="M13 2.5 4.5 13.5H11l-1 8 8.5-11H12z"/>';
  I["bell"] = '<path class="f" d="M6 16.5V11a6 6 0 0 1 12 0v5.5l1.5 2h-15z"/><path d="M10 21h4"/>';
  I["bell-ring"] = I["bell"] + '<path d="M3 8.5a8 8 0 0 1 2.5-4.5M21 8.5a8 8 0 0 0-2.5-4.5"/>';
  I["bell-off"] = '<path d="M8.6 5.6A6 6 0 0 1 18 11v4M6 11v5.5l-1.5 2h13M10 21h4M3.5 3.5l17 17"/>';
  I["chat"] = '<path class="f" d="M4 5.5A1.5 1.5 0 0 1 5.5 4h13A1.5 1.5 0 0 1 20 5.5v9.5a1.5 1.5 0 0 1-1.5 1.5H10l-4.5 4v-4h0A1.5 1.5 0 0 1 4 15z"/><path d="M8 9h8M8 12.5h5"/>';
  I["sparkles"] = '<path class="f" d="M10 3.5l1.6 4.4L16 9.5l-4.4 1.6L10 15.5l-1.6-4.4L4 9.5l4.4-1.6z"/><path d="M17.5 14l.8 2.2 2.2.8-2.2.8-.8 2.2-.8-2.2-2.2-.8 2.2-.8z"/>';
  I["trash"] = '<path d="M4 6.5h16M9.5 6.5V4.5h5v2"/><path class="f" d="M6 6.5l1 13a1.5 1.5 0 0 0 1.5 1.5h7a1.5 1.5 0 0 0 1.5-1.5l1-13"/><path d="M10 10.5v6.5M14 10.5v6.5"/>';
  I["pencil"] = '<path class="f" d="M15.5 4.5l4 4L9 19l-5 1 1-5z"/><path d="M13.5 6.5l4 4"/>';
  I["search"] = '<circle class="f" cx="10.5" cy="10.5" r="6"/><path d="m15 15 5 5"/>';
  I["filter"] = '<path class="f" d="M4 5h16l-6.2 7.5V19l-3.6 1.8v-8.3z"/>';
  I["calendar"] = '<rect class="f" x="3.5" y="5" width="17" height="15.5" rx="2"/><path d="M3.5 10h17M8 3v4M16 3v4"/><circle class="s" cx="8.5" cy="14.5" r="1.1"/><circle class="s" cx="12" cy="14.5" r="1.1"/>';
  I["clock"] = '<circle class="f" cx="12" cy="12" r="8.5"/><path d="M12 7v5l3.5 2"/>';
  I["info"] = '<circle class="f" cx="12" cy="12" r="8.5"/><path d="M12 11v5.5"/><circle class="s" cx="12" cy="7.8" r="1.2"/>';
  I["alert"] = '<path class="f" d="M10.6 4.3a1.6 1.6 0 0 1 2.8 0l7.6 13.4a1.6 1.6 0 0 1-1.4 2.3H4.4A1.6 1.6 0 0 1 3 17.7z"/><path d="M12 9.5v4.5"/><circle class="s" cx="12" cy="16.8" r="1.1"/>';
  I["alert-circle"] = '<circle class="f" cx="12" cy="12" r="8.5"/><path d="M12 7.5v5.5"/><circle class="s" cx="12" cy="16.3" r="1.2"/>';
  I["check-circle"] = '<circle class="f" cx="12" cy="12" r="8.5"/><path d="m8 12.3 2.8 2.8L16.3 9"/>';
  I["landmark"] = '<path class="f" d="M3.5 9 12 4l8.5 5z"/><path d="M5.5 9.5v8M10 9.5v8M14 9.5v8M18.5 9.5v8M3.5 20.5h17M4.5 17.5h15"/>';
  I["banknote"] = '<rect class="f" x="2.5" y="6" width="19" height="12" rx="1.8"/><circle cx="12" cy="12" r="2.6"/><path d="M6 9.5v.01M18 14.5v.01"/>';
  I["credit-card"] = '<rect class="f" x="2.5" y="5" width="19" height="14" rx="2"/><path d="M2.5 9.5h19M6 15h4"/>';
  I["piggy"] = '<path class="f" d="M5 11.5a6.5 5.5 0 0 1 12.2-2.7h1.3l1.5-1.8v3.6l1 1v3.4l-1.8.5-1.3 2v2.5h-2.6v-1.6a7.5 7.5 0 0 1-3.3 0v1.6H9.4v-2.4A5.5 5.5 0 0 1 5 11.5z"/><path d="M9 7.6a5 5 0 0 1 4-.4"/><circle class="s" cx="16.5" cy="11" r="1"/><path d="M5 11.5H3.5"/>';
  I["coins"] = '<ellipse class="f" cx="9" cy="7" rx="5.5" ry="2.5"/><path d="M3.5 7v4c0 1.4 2.5 2.5 5.5 2.5M3.5 11v4c0 1.4 2.5 2.5 5.5 2.5"/><ellipse class="f" cx="15" cy="13" rx="5.5" ry="2.5"/><path d="M9.5 13v4c0 1.4 2.5 2.5 5.5 2.5s5.5-1.1 5.5-2.5v-4"/>';
  I["receipt"] = '<path class="f" d="M5.5 3.5h13v17l-2.2-1.4-2.1 1.4-2.2-1.4-2.2 1.4-2.1-1.4-2.2 1.4z"/><path d="M9 8h6M9 11.5h6M9 15h3.5"/>';
  I["calculator"] = '<rect class="f" x="5" y="3" width="14" height="18" rx="2"/><rect x="8" y="6" width="8" height="3.5" rx=".6"/><circle class="s" cx="9" cy="13" r="1"/><circle class="s" cx="12" cy="13" r="1"/><circle class="s" cx="15" cy="13" r="1"/><circle class="s" cx="9" cy="17" r="1"/><circle class="s" cx="12" cy="17" r="1"/><circle class="s" cx="15" cy="17" r="1"/>';
  I["percent"] = '<path d="M18.5 5.5l-13 13"/><circle class="f" cx="7" cy="7" r="2.5"/><circle class="f" cx="17" cy="17" r="2.5"/>';
  I["download"] = '<path d="M12 3.5v11m-4.5-4.5L12 14.5l4.5-4.5"/><path class="f" d="M4 15.5v3a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-3"/>';
  I["upload"] = '<path d="M12 14.5v-11M7.5 8 12 3.5 16.5 8"/><path class="f" d="M4 15.5v3a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-3"/>';
  I["file"] = '<path class="f" d="M6 3h8.5L19 7.5V19.5A1.5 1.5 0 0 1 17.5 21h-11A1.5 1.5 0 0 1 5 19.5V4.5A1.5 1.5 0 0 1 6 3z"/><path d="M14 3v5h5M8.5 12.5h7M8.5 16h5"/>';
  I["log-out"] = '<path d="M14.5 4.5h3.5a1.5 1.5 0 0 1 1.5 1.5v12a1.5 1.5 0 0 1-1.5 1.5h-3.5"/><path d="M10 16.5 14.5 12 10 7.5M14.5 12H4"/>';
  I["user"] = '<circle class="f" cx="12" cy="8" r="4"/><path class="f" d="M4.5 20.5a7.5 7.5 0 0 1 15 0z"/>';
  I["users"] = '<circle class="f" cx="9" cy="8.5" r="3.5"/><path class="f" d="M2.5 20a6.5 6.5 0 0 1 13 0z"/><path d="M15.5 5.2a3.5 3.5 0 0 1 0 6.6M18 14.3a6.5 6.5 0 0 1 3.5 5.7"/>';
  I["mail"] = '<rect class="f" x="3" y="5.5" width="18" height="13" rx="2"/><path d="m3.5 7 8.5 6 8.5-6"/>';
  I["lock"] = '<rect class="f" x="4.5" y="10.5" width="15" height="10" rx="2"/><path d="M8 10.5V7.5a4 4 0 0 1 8 0v3"/><circle class="s" cx="12" cy="15.5" r="1.3"/>';
  I["key"] = '<circle class="f" cx="8" cy="15" r="4.5"/><path d="M11.3 11.7 20 3M16.5 6.5l2.5 2.5M14 9l2 2"/>';
  I["eye"] = '<path class="f" d="M2.5 12S6 5.5 12 5.5 21.5 12 21.5 12 18 18.5 12 18.5 2.5 12 2.5 12z"/><circle cx="12" cy="12" r="3"/>';
  I["eye-off"] = '<path d="M9.9 5.8A9 9 0 0 1 12 5.5c6 0 9.5 6.5 9.5 6.5a16 16 0 0 1-2.4 3.2M6.5 7.2C3.9 9 2.5 12 2.5 12S6 18.5 12 18.5a9 9 0 0 0 4.4-1.1M3.5 3.5l17 17"/><path d="M9.9 9.9a3 3 0 0 0 4.2 4.2"/>';
  I["inbox"] = '<path class="f" d="M3.5 13.5 6 5.5h12l2.5 8V19a1.5 1.5 0 0 1-1.5 1.5H5A1.5 1.5 0 0 1 3.5 19z"/><path d="M3.5 13.5H8l1.5 2.5h5l1.5-2.5h4.5"/>';
  I["history"] = '<path d="M4 12a8 8 0 1 0 2.4-5.7L4 8.5"/><path d="M4 4v4.5h4.5M12 7.5V12l3 2"/>';
  I["grid"] = '<rect class="f" x="4" y="4" width="6.5" height="6.5" rx="1.5"/><rect x="13.5" y="4" width="6.5" height="6.5" rx="1.5"/><rect x="4" y="13.5" width="6.5" height="6.5" rx="1.5"/><rect class="f" x="13.5" y="13.5" width="6.5" height="6.5" rx="1.5"/>';
  I["list"] = '<path d="M9 6.5h11M9 12h11M9 17.5h11"/><circle class="s" cx="4.8" cy="6.5" r="1.3"/><circle class="s" cx="4.8" cy="12" r="1.3"/><circle class="s" cx="4.8" cy="17.5" r="1.3"/>';
  I["scan"] = '<path d="M4 8V5.5A1.5 1.5 0 0 1 5.5 4H8M16 4h2.5A1.5 1.5 0 0 1 20 5.5V8M20 16v2.5a1.5 1.5 0 0 1-1.5 1.5H16M8 20H5.5A1.5 1.5 0 0 1 4 18.5V16"/><path class="f" d="M7.5 8.5h9v7h-9z"/><path d="M9.5 11h5M9.5 13.5h3"/>';
  I["camera"] = '<path class="f" d="M3.5 8.5A1.5 1.5 0 0 1 5 7h2.5l1.5-2.5h6L16.5 7H19a1.5 1.5 0 0 1 1.5 1.5v10A1.5 1.5 0 0 1 19 20H5a1.5 1.5 0 0 1-1.5-1.5z"/><circle cx="12" cy="13.2" r="3.6"/>';
  I["loader"] = '<path d="M12 3.5a8.5 8.5 0 1 0 8.5 8.5"/>';
  I["more"] = '<circle class="s" cx="5.5" cy="12" r="1.8"/><circle class="s" cx="12" cy="12" r="1.8"/><circle class="s" cx="18.5" cy="12" r="1.8"/>';
  I["target"] = '<circle class="f" cx="12" cy="12" r="8.5"/><circle cx="12" cy="12" r="5"/><circle class="s" cx="12" cy="12" r="1.8"/>';
  I["book"] = '<path class="f" d="M4 5a1.5 1.5 0 0 1 1.5-1.5H19V18H5.8A1.8 1.8 0 0 0 4 19.8z"/><path d="M4 19.8A1.8 1.8 0 0 0 5.8 21.5H19V18M8.5 7.5h6"/>';
  I["book-open"] = '<path class="f" d="M12 6.5C10 5 7 4.5 3 4.8V18.5c4-.3 7 .2 9 1.7 2-1.5 5-2 9-1.7V4.8c-4-.3-7 .2-9 1.7z"/><path d="M12 6.5v13.7"/>';
  I["contact"] = '<rect class="f" x="4.5" y="3" width="15" height="18" rx="2"/><circle cx="12" cy="10" r="2.8"/><path d="M8 17a4 4 0 0 1 8 0M2.8 7.5h3M2.8 12h3M2.8 16.5h3"/>';
  I["save"] = '<path class="f" d="M5 3.5h11l3.5 3.5v12a1.5 1.5 0 0 1-1.5 1.5H5A1.5 1.5 0 0 1 3.5 19V5A1.5 1.5 0 0 1 5 3.5z"/><path d="M7.5 3.5v5h7v-5M7.5 20.5v-6h9v6"/>';
  I["shield"] = '<path class="f" d="M12 3 4.5 6v6c0 4.5 3.2 7.8 7.5 9 4.3-1.2 7.5-4.5 7.5-9V6z"/>';
  I["shield-check"] = I["shield"] + '<path d="m8.8 12 2.3 2.3 4.2-4.6"/>';
  I["star"] = '<path class="f" d="m12 3.5 2.6 5.4 5.9.8-4.3 4.1 1 5.8L12 16.8l-5.2 2.8 1-5.8-4.3-4.1 5.9-.8z"/>';
  I["heart"] = '<path class="f" d="M12 20.3s-8.5-4.8-8.5-10.8A4.7 4.7 0 0 1 12 6.8a4.7 4.7 0 0 1 8.5 2.7c0 6-8.5 10.8-8.5 10.8z"/>';
  I["google"] = '<path d="M20.5 12.2c0-.6-.1-1.2-.2-1.7H12v3.3h4.8a4.2 4.2 0 0 1-1.8 2.7"/><path d="M15 16.5A7 7 0 1 1 16.8 7"/>';
  I["power"] = '<path d="M12 3v8"/><path d="M7 6.3a7.5 7.5 0 1 0 10 0"/>';
  I["image"] = '<rect class="f" x="3.5" y="4.5" width="17" height="15" rx="2"/><circle class="s" cx="9" cy="9.5" r="1.6"/><path d="m3.5 17 5-5 4 4 2.5-2.5 5.5 5"/>';
  I["flower"] = '<circle class="f" cx="12" cy="7" r="3.3"/><circle class="f" cx="16.8" cy="10.5" r="3.3"/><circle class="f" cx="15" cy="16.2" r="3.3"/><circle class="f" cx="9" cy="16.2" r="3.3"/><circle class="f" cx="7.2" cy="10.5" r="3.3"/><circle class="s" cx="12" cy="12" r="2"/>';
  I["globe"] = '<circle class="f" cx="12" cy="12" r="8.5"/><path d="M3.5 12h17M12 3.5c2.3 2.4 3.4 5.2 3.4 8.5s-1.1 6.1-3.4 8.5c-2.3-2.4-3.4-5.2-3.4-8.5S9.7 5.9 12 3.5z"/>';

  // ---------- Ăn uống ----------
  I["coffee"] = '<path class="f" d="M4.5 9.5h12v5a5 5 0 0 1-5 5h-2a5 5 0 0 1-5-5z"/><path d="M16.5 10.5h1.3a2.5 2.5 0 0 1 0 5h-1.6M3.5 21h15M8 3.5c-.8 1 .8 2 0 3.5M12 3.5c-.8 1 .8 2 0 3.5"/>';
  I["phin"] = '<path class="f" d="M7 4.5h10l-1 6.5H8z"/><path d="M5 4.5h14M5.5 11h13M9 11l-.5 2.5h7L15 11"/><path class="f" d="M7.5 15.5h9l-1 5h-7z"/>';
  I["milk-tea"] = '<path class="f" d="M6.5 8.5h11l-1.4 11.2a1.5 1.5 0 0 1-1.5 1.3H9.4a1.5 1.5 0 0 1-1.5-1.3z"/><path d="M5.5 8.5h13M8 8.5a4 4 0 0 1 8 0M13.5 2.5 12 8.5"/><circle class="s" cx="10" cy="17.5" r="1"/><circle class="s" cx="13" cy="18" r="1"/><circle class="s" cx="11.8" cy="15.5" r="1"/>';
  I["utensils"] = '<path d="M6 3v7a2.5 2.5 0 0 0 5 0V3M8.5 3v18"/><path class="f" d="M17.5 3c-2 1.5-2.5 4-2.5 7v3h2.5v8"/>';
  I["pho"] = '<path class="f" d="M3 11.5h18a9 9 0 0 1-18 0z"/><path d="M8 20.5h8M15 3.5 9 11M19.5 4.5 11 11"/><path d="M7 9c1-1 2 1 3 0s2 1 3 0"/>';
  I["rice"] = '<path class="f" d="M4 12h16a8 8 0 0 1-16 0z"/><path class="f" d="M6 12a6 4.5 0 0 1 12 0"/><path d="M9.5 20.5h5"/>';
  I["banh-mi"] = '<path class="f" d="M3.8 15.5C3 12.8 6.5 8 13 6.5s8.5.5 7.6 3.3c-.8 2.6-5 6.2-11.3 7.6-3 .6-5 .3-5.5-1.9z"/><path d="M9 10.5l1.5 2.5M12.5 9l1.5 2.5M16 8l1.3 2.2"/>';
  I["cup-soda"] = '<path class="f" d="M6.5 8h11l-1.3 12.2a1.5 1.5 0 0 1-1.5 1.3H9.3a1.5 1.5 0 0 1-1.5-1.3z"/><path d="M5.5 8h13M12 8l1.5-5.5 3 .5M7 13h10"/>';
  I["beer"] = '<path class="f" d="M5.5 7.5h10v12a1.5 1.5 0 0 1-1.5 1.5H7a1.5 1.5 0 0 1-1.5-1.5z"/><path d="M15.5 10h1.8a2 2 0 0 1 2 2v3.5a2 2 0 0 1-2 2h-1.8M5.5 7.5a2.3 2.3 0 0 1 1.7-3.9 2.8 2.8 0 0 1 5 0A2.3 2.3 0 0 1 15.5 7.5M9 11v6.5M12 11v6.5"/>';
  I["wine"] = '<path class="f" d="M7.5 3.5h9l.5 5a5 5 0 0 1-10 0z"/><path d="M12 13.5v7M8.5 20.5h7M7.3 8h9.4"/>';
  I["cake"] = '<path class="f" d="M4 12.5h16v8H4z"/><path d="M4 15.5c1.3 1 2.7 1 4 0s2.7-1 4 0 2.7 1 4 0 2.7-1 4 0M12 12.5V9M8 12.5V10M16 12.5V10M3 20.5h18"/><path class="s" d="M12 4.5c.9 1 .9 2.3 0 3-.9-.7-.9-2 0-3z"/>';
  I["ice-cream"] = '<path class="f" d="M7 11a5 5 0 0 1 10 0z"/><path d="M7 11l5 10.5L17 11"/><path d="M9.5 14.5l4.2 2.8M14.7 13.7l-5 3.6"/>';
  I["pizza"] = '<path class="f" d="M3.5 6.5C9 3.5 15 3.5 20.5 6.5L12 21z"/><path d="M4.8 9.5c4.6-2.2 9.8-2.2 14.4 0"/><circle class="s" cx="10" cy="12" r="1.2"/><circle class="s" cx="14" cy="13.5" r="1.2"/><circle class="s" cx="12" cy="16.5" r="1.1"/>';
  I["apple"] = '<path class="f" d="M12 7.5c-1.5-1.3-6.5-1.8-7.3 3.5-.6 4.3 2.6 10 5.2 9.5 1.2-.2 1.3-.7 2.1-.7s.9.5 2.1.7c2.6.5 5.8-5.2 5.2-9.5-.8-5.3-5.8-4.8-7.3-3.5z"/><path d="M12 7.5c0-2 .8-3.5 2.5-4"/>';
  I["cart"] = '<path d="M2.5 3.5h2.7l2.3 11.5h11l2-8H6.3"/><path class="d" d="M6.3 7h14.2l-2 8h-11z"/><circle class="s" cx="9" cy="19.5" r="1.6"/><circle class="s" cx="17" cy="19.5" r="1.6"/>';
  I["basket"] = '<path class="f" d="M3.5 10h17l-1.8 9a1.5 1.5 0 0 1-1.5 1.2H6.8a1.5 1.5 0 0 1-1.5-1.2z"/><path d="m8 10 3-6.5M16 10l-3-6.5M9 13.5v4M12 13.5v4M15 13.5v4"/>';
  I["fish"] = '<path class="f" d="M3 12c3-5 9-6.5 13.5-2.3L21 6v12l-4.5-3.7C12 18.5 6 17 3 12z"/><circle class="s" cx="8" cy="11" r="1.1"/>';
  I["egg"] = '<path class="f" d="M12 3c-4 0-6.5 7-6.5 10.5a6.5 6.5 0 0 0 13 0C18.5 10 16 3 12 3z"/>';
  I["carrot"] = '<path class="f" d="M14.5 9.5 4 20.5c-.5.5 9.7-3.7 11.6-5.6a3.8 3.8 0 0 0-1.1-5.4z"/><path d="M14.5 9.5c.5-2.5 2-4 4-4.5M15.5 10.5c2.3-.6 4-.2 5 1M15 10c0-2.5.8-4.5 2-6M8 15.5l1.5 1.5M10.5 13l1.5 1.5"/>';
  I["candy"] = '<circle class="f" cx="12" cy="12" r="4"/><path d="M8.5 9.8 3.5 8l1 4 4.1.3M15.5 14.2l5 1.8-1-4-4.1-.3"/>';
  I["chef"] = '<path class="f" d="M7 13.5a4 4 0 0 1-1-7.8 4.5 4.5 0 0 1 8.4-1.7 4 4 0 0 1 3.6 1.7A4 4 0 0 1 17 13.5V20H7z"/><path d="M7 16.5h10"/>';

  // ---------- Di chuyển ----------
  I["scooter"] = '<circle class="f" cx="5.5" cy="17" r="2.8"/><circle class="f" cx="18.5" cy="17" r="2.8"/><path d="M5.5 17h6l2-5.5H9.5a3 3 0 0 0-3 3M13.5 11.5 16 6h2.5M16 6l2.5 11"/><path class="d" d="M8 11.5h6l-1.5 5.5H8z"/>';
  I["bike"] = '<circle class="f" cx="5.5" cy="16.5" r="3.5"/><circle class="f" cx="18.5" cy="16.5" r="3.5"/><path d="M5.5 16.5 9.5 9h6l3 7.5M9.5 9l3 7.5h-7M15.5 9l1-3h2"/>';
  I["car"] = '<path class="f" d="M3.5 16v-3.5l2-5A2 2 0 0 1 7.4 6h9.2a2 2 0 0 1 1.9 1.5l2 5V16a1 1 0 0 1-1 1h-15a1 1 0 0 1-1-1z"/><path d="M4 12.5h16M6.5 17v2.5M17.5 17v2.5"/><circle class="s" cx="7.5" cy="14.5" r="1"/><circle class="s" cx="16.5" cy="14.5" r="1"/>';
  I["taxi"] = I["car"] + '<path d="M10 3.5h4v2.5h-4z"/>';
  I["bus"] = '<rect class="f" x="4.5" y="3.5" width="15" height="15" rx="2.5"/><path d="M4.5 11h15M8 18.5v2M16 18.5v2M8 7h8"/><circle class="s" cx="8" cy="14.8" r="1"/><circle class="s" cx="16" cy="14.8" r="1"/>';
  I["train"] = '<rect class="f" x="5.5" y="3" width="13" height="14" rx="3"/><path d="M5.5 10h13M8.5 20.5l2-3.5M15.5 20.5l-2-3.5"/><circle class="s" cx="9" cy="13.5" r="1"/><circle class="s" cx="15" cy="13.5" r="1"/>';
  I["plane"] = '<path class="f" d="M21 4.2c-.6-.6-1.6-.6-2.4.1L14.8 8 5.5 5.5 3.8 7.2l7.4 4.3-3.4 3.4-2.7-.4-1.3 1.3 3.6 1.6 1.6 3.6 1.3-1.3-.4-2.7 3.4-3.4 4.3 7.4 1.7-1.7L16.2 9.2l3.7-3.8c.7-.8.7-1.8.1-2.4z"/>';
  I["ship"] = '<path class="f" d="M3 14.5h18l-2.5 5H5.5z"/><path d="M5.5 14.5v-5h13v5M9 9.5V5.5h6v4M12 3v2.5"/>';
  I["fuel"] = '<path class="f" d="M4.5 20.5V5A1.5 1.5 0 0 1 6 3.5h7A1.5 1.5 0 0 1 14.5 5v15.5z"/><path d="M3 20.5h13M7 8h5M14.5 11h1.8a1.5 1.5 0 0 1 1.5 1.5v4a1.3 1.3 0 0 0 2.6 0V8.5L17.5 6"/>';
  I["parking"] = '<rect class="f" x="3.5" y="3.5" width="17" height="17" rx="3.5"/><path d="M9.5 17V7.5h3.3a2.7 2.7 0 0 1 0 5.5H9.5"/>';
  I["wrench"] = '<path class="f" d="M14.5 3.8a5 5 0 0 0-5.8 6.6l-5.3 5.3a2 2 0 0 0 2.9 2.9l5.3-5.3a5 5 0 0 0 6.6-5.8l-3 3-2.7-.7-.7-2.7z"/>';
  I["map-pin"] = '<path class="f" d="M12 21s-6.5-6-6.5-11a6.5 6.5 0 0 1 13 0c0 5-6.5 11-6.5 11z"/><circle cx="12" cy="10" r="2.4"/>';
  I["compass"] = '<circle class="f" cx="12" cy="12" r="8.5"/><path d="m15.5 8.5-2 5-5 2 2-5z"/>';
  I["luggage"] = '<rect class="f" x="5.5" y="7" width="13" height="12.5" rx="2"/><path d="M9.5 7V4.5h5V7M9.5 10.5v6M14.5 10.5v6M8 19.5v1.5M16 19.5v1.5"/>';
  I["ticket"] = '<path class="f" d="M3.5 7a1.5 1.5 0 0 1 1.5-1.5h14A1.5 1.5 0 0 1 20.5 7v2.5a2.5 2.5 0 0 0 0 5V17a1.5 1.5 0 0 1-1.5 1.5H5A1.5 1.5 0 0 1 3.5 17v-2.5a2.5 2.5 0 0 0 0-5z"/><path d="M14.5 5.5v2M14.5 11v2M14.5 16.5v2"/>';
  I["road"] = '<path class="f" d="M8 3.5h8l3.5 17h-15z"/><path d="M12 5.5v2.5M12 11v2.5M12 16.5v3"/>';

  // ---------- Nhà cửa ----------
  I["building"] = '<path class="f" d="M5 20.5V5a1.5 1.5 0 0 1 1.5-1.5h7A1.5 1.5 0 0 1 15 5v15.5"/><path d="M15 9.5h3.5A1.5 1.5 0 0 1 20 11v9.5M3 20.5h18M8.5 7.5h2.5M8.5 11h2.5M8.5 14.5h2.5"/>';
  I["bolt"] = I["zap"];
  I["droplet"] = '<path class="f" d="M12 3s-6.5 7-6.5 11.2a6.5 6.5 0 0 0 13 0C18.5 10 12 3 12 3z"/><path d="M9 14.5a3 3 0 0 0 3 3"/>';
  I["wifi"] = '<path d="M2.5 9a14 14 0 0 1 19 0M5.5 12.5a9.5 9.5 0 0 1 13 0M8.5 16a5 5 0 0 1 7 0"/><circle class="s" cx="12" cy="19.2" r="1.4"/>';
  I["tv"] = '<rect class="f" x="3" y="6.5" width="18" height="12" rx="2"/><path d="m8.5 3 3.5 3.5L15.5 3M8 21.5h8"/>';
  I["bulb"] = '<path class="f" d="M9 16.5c0-2-3.5-3.5-3.5-7.5a6.5 6.5 0 0 1 13 0c0 4-3.5 5.5-3.5 7.5z"/><path d="M9.5 19.5h5M10.5 22h3"/>';
  I["bed"] = '<path class="f" d="M3 17V9.5h7.5a1 1 0 0 1 1 1V13H21v4z"/><path d="M3 5v15M21 13v7M3 17h18"/><circle cx="6.8" cy="11.2" r="1.6"/>';
  I["bath"] = '<path class="f" d="M3 12h18v2.5a5 5 0 0 1-5 5H8a5 5 0 0 1-5-5z"/><path d="M6 12V5.5A2 2 0 0 1 8 3.5a2 2 0 0 1 2 2M7.5 19.5 6.5 21.5M16.5 19.5l1 2"/>';
  I["sofa"] = '<path class="f" d="M5 11V8a2.5 2.5 0 0 1 2.5-2.5h9A2.5 2.5 0 0 1 19 8v3"/><path class="f" d="M3 13a2 2 0 0 1 4 0v1.5h10V13a2 2 0 0 1 4 0v5H3z"/><path d="M5 18v2.5M19 18v2.5"/>';
  I["fan"] = '<circle class="s" cx="12" cy="10" r="1.6"/><path class="f" d="M12 10c-1-3.5 0-6.5 3-6.5s3 4-3 6.5zM12 10c3.5-1 6.5 0 6.5 3s-4 3-6.5-3zM12 10c1 3.5 0 6.5-3 6.5s-3-4 3-6.5zM12 10c-3.5 1-6.5 0-6.5-3s4-3 6.5 3z"/><path d="M12 16.5v4M8.5 20.5h7"/>';
  I["plug"] = '<path class="f" d="M7 8.5h10v3a5 5 0 0 1-10 0z"/><path d="M9.5 8.5V3.5M14.5 8.5V3.5M12 16.5v5"/>';
  I["flame"] = '<path class="f" d="M12 21.5a6.5 6.5 0 0 1-6.5-6.5c0-4 3.5-6 3.5-10 2 1 3.5 3 3.7 5 .8-.8 1.3-2 1.3-3 2.5 2 4.5 5 4.5 8a6.5 6.5 0 0 1-6.5 6.5z"/><path d="M12 21.5a2.5 2.5 0 0 1-2.5-2.5c0-1.8 2.5-3.5 2.5-3.5s2.5 1.7 2.5 3.5a2.5 2.5 0 0 1-2.5 2.5z"/>';
  I["tree"] = '<path class="f" d="M12 2.5 5.5 11h3l-3.5 5h14l-3.5-5h3z"/><path d="M12 16v5.5"/>';
  I["sprout"] = '<path class="f" d="M12 13C12 9 9 6 4 6c0 4.5 3 7 8 7zM12 10.5c0-3.5 2.7-6 7.5-6 0 4.2-2.7 6.5-7.5 6.5"/><path d="M12 21v-10.5M8 21h8"/>';
  I["key-house"] = I["key"];
  I["trash-can"] = I["trash"];

  // ---------- Mua sắm ----------
  I["bag"] = '<path class="f" d="M5 7.5h14l1 13H4z"/><path d="M8.5 10V6.5a3.5 3.5 0 0 1 7 0V10"/>';
  I["shirt"] = '<path class="f" d="M8.5 3.5 3 6.5l2 4 2-1v11h10v-11l2 1 2-4-5.5-3c-.5 1.5-2 2.5-3.5 2.5S9 5 8.5 3.5z"/>';
  I["ao-dai"] = '<path class="f" d="M9.5 3.5h5l3 4-1.5 2-1-.8L16.5 21h-9l2-12.3-1 .8-1.5-2z"/><path d="M12 3.5v3M10 12l4 0"/>';
  I["shoe"] = '<path class="f" d="M3 16.5V8.5h4.5c1 2.5 3 3.5 5.5 3.5l6 1.5a2.5 2.5 0 0 1 2 2.5v.5z"/><path d="M3 16.5v2h18v-2M9 9.5l1.5-1.5M11 11l1.5-1.5"/>';
  I["watch"] = '<circle class="f" cx="12" cy="12" r="5.5"/><path d="M9 7l.7-3.5h4.6L15 7M9 17l.7 3.5h4.6L15 17M12 10v2.3l1.5 1"/>';
  I["glasses"] = '<circle class="f" cx="6.5" cy="14.5" r="3.5"/><circle class="f" cx="17.5" cy="14.5" r="3.5"/><path d="M10 14.5c1.3-1 2.7-1 4 0M3 14.5 4.5 7h2M21 14.5 19.5 7h-2"/>';
  I["gem"] = '<path class="f" d="M6.5 4h11l3.5 5-9 11.5L3 9z"/><path d="M3 9h18M9.5 4 8 9l4 11.5L16 9l-1.5-5"/>';
  I["gift"] = '<rect class="f" x="4" y="10" width="16" height="10.5" rx="1.5"/><path d="M3 7h18v3H3zM12 7v13.5M12 7c-1-3.5-5.5-4-5.5-1.5S12 7 12 7zm0 0c1-3.5 5.5-4 5.5-1.5S12 7 12 7z"/>';
  I["scissors"] = '<circle class="f" cx="6.5" cy="6.5" r="3"/><circle class="f" cx="6.5" cy="17.5" r="3"/><path d="M9 8.5 20 18M9 15.5 20 6"/>';
  I["package"] = '<path class="f" d="M12 3 20 7v10l-8 4-8-4V7z"/><path d="M4 7l8 4 8-4M12 11v10M8 5l8 4"/>';
  I["lipstick"] = '<path class="f" d="M9 11h6v10H9z"/><path d="M10 11V6.5l4-3V11M8 21h8"/>';
  I["store"] = '<path class="f" d="M4.5 11v9.5h15V11"/><path d="M3 9.5 5 4h14l2 5.5a2.5 2.5 0 0 1-4.5 1.5 2.5 2.5 0 0 1-4.5 0 2.5 2.5 0 0 1-4.5 0A2.5 2.5 0 0 1 3 9.5zM9.5 20.5v-5h5v5"/>';
  I["crown"] = '<path class="f" d="M3.5 7.5 8 11l4-6.5 4 6.5 4.5-3.5-1.5 11h-14z"/><path d="M5.5 20.5h13"/>';
  I["umbrella"] = '<path class="f" d="M3 12a9 9 0 0 1 18 0c-1.5-1.5-3-1.5-4.5 0-1.5-1.5-3-1.5-4.5 0-1.5-1.5-3-1.5-4.5 0C6 10.5 4.5 10.5 3 12z"/><path d="M12 12v6.5a2 2 0 0 1-4 0M12 3v-.5"/>';
  I["non-la"] = '<path class="f" d="M12 4 2.5 15.5c3 1.5 6 2 9.5 2s6.5-.5 9.5-2z"/><path d="M7 17.2 12 4l5 13.2M2.5 15.5h19"/>';

  // ---------- Công nghệ ----------
  I["phone"] = '<rect class="f" x="6" y="2.5" width="12" height="19" rx="2.5"/><path d="M10.5 5.5h3"/><circle class="s" cx="12" cy="18" r="1.1"/>';
  I["laptop"] = '<rect class="f" x="4.5" y="4.5" width="15" height="10.5" rx="1.5"/><path d="M2.5 18.5h19l-1.5-3.5H4z"/>';
  I["headphones"] = '<path d="M4 15v-3a8 8 0 0 1 16 0v3"/><rect class="f" x="3" y="13.5" width="4.5" height="7" rx="1.8"/><rect class="f" x="16.5" y="13.5" width="4.5" height="7" rx="1.8"/>';
  I["gamepad"] = '<path class="f" d="M7 7h10a4.5 4.5 0 0 1 4.4 5.4l-.9 4.6a2.4 2.4 0 0 1-4.1 1.2L14.5 16h-5l-1.9 2.2a2.4 2.4 0 0 1-4.1-1.2l-.9-4.6A4.5 4.5 0 0 1 7 7z"/><path d="M7.5 10v4M5.5 12h4"/><circle class="s" cx="15.5" cy="11" r="1.1"/><circle class="s" cx="17.5" cy="13" r="1.1"/>';
  I["monitor"] = '<rect class="f" x="3" y="4" width="18" height="12.5" rx="1.8"/><path d="M12 16.5v4M8 20.5h8"/>';
  I["printer"] = '<path class="f" d="M5 9h14a2 2 0 0 1 2 2v6h-3.5M6.5 17H3v-6a2 2 0 0 1 2-2"/><path d="M7 9V3.5h10V9M7 14h10v7H7z"/>';
  I["radio"] = '<rect class="f" x="3" y="8" width="18" height="12.5" rx="2"/><path d="M6 8l11-4.5"/><circle cx="15.5" cy="14.2" r="3"/><path d="M6.5 12h4M6.5 15h4"/>';
  I["cassette"] = '<rect class="f" x="2.5" y="5.5" width="19" height="13" rx="2"/><circle cx="8" cy="11" r="2"/><circle cx="16" cy="11" r="2"/><path d="M10 11h4M6.5 18.5l1.5-3h8l1.5 3"/>';

  // ---------- Tài chính (thêm) ----------
  I["briefcase"] = '<rect class="f" x="3" y="7" width="18" height="13" rx="2"/><path d="M8.5 7V5a1.5 1.5 0 0 1 1.5-1.5h4A1.5 1.5 0 0 1 15.5 5v2M3 12.5h18M10.5 12.5v2h3v-2"/>';
  I["hand-coins"] = '<circle class="f" cx="15.5" cy="6.5" r="3.5"/><path d="M2.5 13.5h3l3.5-1.5h4a1.5 1.5 0 0 1 0 3H10m3.5 0 5-2.5a1.6 1.6 0 0 1 1.8 2.6L15 19.5H5.5L2.5 21"/>';
  I["safe"] = '<rect class="f" x="3.5" y="3.5" width="17" height="15" rx="2"/><circle cx="12" cy="11" r="3.5"/><path d="M12 7.5V9M12 13v1.5M6.5 18.5v2M17.5 18.5v2"/>';
  I["handshake"] = '<path class="f" d="m3 11 4.5-5 4 2 4-2L21 11l-6 6.5c-.8.8-1.8.8-2.5 0L3 11z"/><path d="m11.5 8-3 3a1.4 1.4 0 0 0 2 2l2-2M12.5 15l1-1M10.5 13.5l1-1"/>';

  // ---------- Sức khỏe ----------
  I["heart-pulse"] = I["heart"] + '<path d="M5 12.5h3.5l1.5-3 2.5 5.5 1.5-2.5H19"/>';
  I["pill"] = '<path class="f" d="m4.9 12.4 7.5-7.5a4.6 4.6 0 0 1 6.6 6.6l-7.5 7.5a4.6 4.6 0 0 1-6.6-6.6z"/><path d="m8.7 8.7 6.6 6.6"/>';
  I["stethoscope"] = '<path d="M5.5 3.5H4v5a5 5 0 0 0 10 0v-5h-1.5M9 13.5v1.5a5 5 0 0 0 10 0v-2"/><circle class="f" cx="19" cy="11" r="2.3"/>';
  I["dumbbell"] = '<rect class="f" x="2.5" y="9" width="4" height="6" rx="1"/><rect class="f" x="17.5" y="9" width="4" height="6" rx="1"/><rect x="5.5" y="7" width="3" height="10" rx="1"/><rect x="15.5" y="7" width="3" height="10" rx="1"/><path d="M8.5 12h7"/>';
  I["activity"] = '<path d="M2.5 12h4l2.5-6.5 4.5 13 2.5-6.5h5.5"/>';
  I["syringe"] = '<path class="f" d="m14 5 5 5-8.5 8.5-5-5z"/><path d="m17 3 4 4M19 5l-2 2M5.5 13.5 3 21l7.5-2.5M10 9.5l2 2M12.5 7l2 2"/>';
  I["tooth"] = '<path class="f" d="M12 5.5C10 3.5 4.5 3 4.5 8c0 3 1.5 4.5 2 7.5.5 3 1 5 2.2 5S10.5 15 12 15s2.1 5.5 3.3 5.5 1.7-2 2.2-5c.5-3 2-4.5 2-7.5 0-5-5.5-4.5-7.5-2.5z"/>';
  I["baby"] = '<circle class="f" cx="12" cy="12.5" r="8"/><path d="M9.5 15.5a3.5 3.5 0 0 0 5 0M12 4.5c-1.5 0-2 2 0 2.5"/><circle class="s" cx="9" cy="11.5" r="1"/><circle class="s" cx="15" cy="11.5" r="1"/>';
  I["cross"] = '<path class="f" d="M9 3.5h6v5.5h5.5v6H15v5.5H9V15H3.5V9H9z"/>';
  I["yoga"] = '<circle class="f" cx="12" cy="4.8" r="2"/><path d="M12 7.5v6M5 10l7 1.5 7-1.5M7.5 20.5l4.5-7 4.5 7M4 20.5h16"/>';

  // ---------- Giải trí ----------
  I["film"] = '<rect class="f" x="3" y="3.5" width="18" height="17" rx="2"/><path d="M7.5 3.5v17M16.5 3.5v17M3 8h4.5M3 12h18M3 16h4.5M16.5 8H21M16.5 16H21"/>';
  I["music"] = '<path d="M9 17.5V5.5l11-2v12"/><circle class="f" cx="6.5" cy="17.5" r="2.8"/><circle class="f" cx="17.5" cy="15.5" r="2.8"/><path d="M9 9.5l11-2"/>';
  I["guitar"] = '<path class="f" d="M10.5 11.5c-2-1-4.5-.5-5.5 1.5-1 1.8-.2 3-1 4.2-.8 1.2.4 3.4 2.8 3.8 2.4.4 3.7-.8 4.4-2.5.6-1.4 1.7-1.6 2.2-2.5 1.2-2-.2-3.6-2.9-4.5z"/><path d="m12 12.5 8-8M18 3l3 3M8.5 15.5l.01.01"/>';
  I["party"] = '<path class="f" d="M3.5 20.5 8 7.5l8.5 8.5z"/><path d="M13 3.5c.5 1.5 0 3-1.5 3.5M20.5 11c-1.5-.5-3 0-3.5 1.5M16 4.5l.01.01M19.5 8l.01.01M17 7l-3 3"/>';
  I["paw"] = '<path class="f" d="M12 12c-3 0-6 3.5-6 6 0 1.6 1.3 2.5 3 2.5 1.2 0 2-.6 3-.6s1.8.6 3 .6c1.7 0 3-.9 3-2.5 0-2.5-3-6-6-6z"/><circle class="f" cx="5.5" cy="10" r="2"/><circle class="f" cx="9.3" cy="5.5" r="2"/><circle class="f" cx="14.7" cy="5.5" r="2"/><circle class="f" cx="18.5" cy="10" r="2"/>';
  I["cat"] = '<path class="f" d="M4.5 10.5V3.5l4.5 3.5h6l4.5-3.5v7c0 5.5-3.5 9-7.5 9s-7.5-3.5-7.5-9z"/><circle class="s" cx="9" cy="12" r="1"/><circle class="s" cx="15" cy="12" r="1"/><path d="M11 15.5h2l-1 1zM2 14l4 .5M22 14l-4 .5"/>';
  I["dog"] = '<path class="f" d="M7.5 8.5c0-2.5 2-4.5 4.5-4.5s4.5 2 4.5 4.5v6.5a4.5 4.5 0 0 1-9 0z"/><path d="M7.5 7 4 5c-1 2-.5 5 1.5 6.5M16.5 7 20 5c1 2 .5 5-1.5 6.5M10.5 16.5h3l-1.5 1.5z"/><circle class="s" cx="10" cy="11" r="1"/><circle class="s" cx="14" cy="11" r="1"/>';
  I["palm"] = '<path d="M12 21.5c0-5 .5-9 2-12.5M8 21.5h8"/><path class="f" d="M14 9c-2.5-3-6.5-3.5-9-1.5C8 7 11 7.5 14 9zM14 9c1-3.5 4.5-5.5 7.5-4.5C19 5.8 16 7 14 9zM14 9c3 0 6 2 7 5-3 0-5.5-1.5-7-5zM14 9c-3 0-6.5 2-7.5 5 3 0 6-1.5 7.5-5z"/>';
  I["tent"] = '<path class="f" d="M12 4 2.5 20h19z"/><path d="M12 4l-1.2-2M12 4l1.2-2M12 11l-3.5 9h7z"/>';
  I["mountain"] = '<path class="f" d="M2.5 20 9 8.5l3.5 5.5 2.5-3.5 6.5 9.5z"/><path d="M7.2 11.8 9 13l1.5-1.5"/>';
  I["dice"] = '<rect class="f" x="3.5" y="3.5" width="17" height="17" rx="3.5"/><circle class="s" cx="8.2" cy="8.2" r="1.3"/><circle class="s" cx="15.8" cy="8.2" r="1.3"/><circle class="s" cx="12" cy="12" r="1.3"/><circle class="s" cx="8.2" cy="15.8" r="1.3"/><circle class="s" cx="15.8" cy="15.8" r="1.3"/>';
  I["vinyl"] = '<circle class="f" cx="12" cy="12" r="8.5"/><circle cx="12" cy="12" r="2.5"/><path d="M12 6.5a5.5 5.5 0 0 0-5.5 5.5M17.5 12a5.5 5.5 0 0 1-5.5 5.5"/>';
  I["disco"] = '<circle class="f" cx="12" cy="13" r="7.5"/><path d="M12 2.5v3M4.5 13h15M12 5.5c-2.5 2-3.5 4.5-3.5 7.5s1 5.5 3.5 7.5c2.5-2 3.5-4.5 3.5-7.5s-1-5.5-3.5-7.5zM6 8.8h12M6 17.2h12"/>';

  // ---------- Học tập ----------
  I["graduation"] = '<path class="f" d="M2.5 9.5 12 5l9.5 4.5L12 14z"/><path d="M6.5 11.5V16c0 1.5 2.5 3 5.5 3s5.5-1.5 5.5-3v-4.5M21.5 9.5v5.5"/>';
  I["pen"] = I["pencil"];
  I["school"] = '<path class="f" d="M5 20.5V10l7-4.5 7 4.5v10.5z"/><path d="M3 20.5h18M12 5.5V2.5h3M10 20.5v-5h4v5"/><circle cx="12" cy="11" r="1.6"/>';
  I["lamp"] = '<path class="f" d="M7 3.5h10l3 7H4z"/><path d="M12 10.5v8M8 20.5h8"/>';

  // ---------- Gia đình & Xã hội ----------
  I["phone-call"] = '<path class="f" d="M5 3.5h3.5l1.5 4.5-2 1.5a11 11 0 0 0 6.5 6.5l1.5-2 4.5 1.5V19a1.5 1.5 0 0 1-1.5 1.5A16.5 16.5 0 0 1 3.5 5 1.5 1.5 0 0 1 5 3.5z"/><path d="M14.5 3.5a6 6 0 0 1 6 6M14.5 7a2.5 2.5 0 0 1 2.5 2.5"/>';
  I["envelope-red"] = '<rect class="f" x="5" y="2.5" width="14" height="19" rx="1.8"/><path d="M5 7.5c2.3 2 4.7 3 7 3s4.7-1 7-3"/><circle cx="12" cy="13" r="2.2"/>';
  I["pagoda"] = '<path class="f" d="M5 9.5h14l-2-3H7zM6.5 14.5h11l-2-3h-7z"/><path d="M12 2.5v4M8 9.5v2M16 9.5v2M8.5 14.5v6h7v-6M4 20.5h16M3.5 9.5h1.5M19 9.5h1.5"/>';
  I["flag"] = '<path class="f" d="M5.5 4c3-1.5 5.5 1.5 8.5 0s4-1 4.5-.5V12c-.5-.5-1.5-1-4.5.5s-5.5-1.5-8.5 0z"/><path d="M5.5 21.5V4"/>';
  I["home-heart"] = I["home"] + '<path class="s" d="M12 17.8s-2.6-1.5-2.6-3.3a1.4 1.4 0 0 1 2.6-.8 1.4 1.4 0 0 1 2.6.8c0 1.8-2.6 3.3-2.6 3.3z"/>';

  // ---------- Thu nhập ----------
  I["salary"] = '<rect class="f" x="3" y="6.5" width="18" height="11" rx="1.8"/><path d="M12 9v6M14 10.2c-.5-.8-3.5-1-3.5.6s3.5.9 3.5 2.6-3 1.4-3.5.5"/>';
  I["chart-up"] = I["trend-up"];

  // ===== Bí danh: tên icon cũ (lucide) trong dữ liệu đã lưu -> icon mới =====
  var A = {
    "layout-dashboard": "home", "bar-chart-2": "chart", "bar-chart-3": "chart", "bar-chart": "chart", "line-chart": "trend-up", "candlestick-chart": "chart", "pie-chart": "pie",
    "trending-up": "trend-up", "trending-down": "trend-down", "wallet-2": "wallet", "wallet-cards": "wallet", "plus-circle": "plus", "circle-plus": "plus",
    "settings-2": "settings", "cog": "settings", "refresh-cw": "refresh", "rotate-ccw": "history", "repeat": "refresh", "message-circle": "chat", "message-square": "chat", "messages-square": "chat",
    "sparkle": "sparkles", "wand-2": "sparkles", "trash-2": "trash", "edit-2": "pencil", "edit-3": "pencil", "pen-tool": "pencil", "pencil-line": "pencil",
    "calendar-days": "calendar", "alarm-clock": "clock", "timer": "clock", "hourglass": "clock", "alert-triangle": "alert", "shield-alert": "alert", "badge-alert": "alert",
    "check-circle-2": "check-circle", "badge-check": "check-circle", "user-check": "check-circle", "check-square": "check-circle", "clipboard-check": "check-circle", "file-check": "file",
    "piggy-bank": "piggy", "vault": "safe", "circle-dollar-sign": "coins", "badge-dollar-sign": "coins", "dollar-sign": "coins", "badge-percent": "percent",
    "arrow-left-right": "swap", "file-text": "file", "file-spreadsheet": "file", "clipboard-list": "file", "newspaper": "file", "file-json": "file",
    "users-2": "users", "user-plus": "user", "user-cog": "user", "contact": "contact", "key-round": "key", "layout-grid": "grid", "list-todo": "list",
    "scan-text": "scan", "scan-line": "scan", "qr-code": "scan", "barcode": "scan", "loader-2": "loader", "more-horizontal": "more", "book-marked": "book", "books": "book", "library": "book",
    "shield-plus": "shield", "shield-check": "shield-check", "crosshair": "target",
    // ăn uống
    "utensils-crossed": "utensils", "soup": "pho", "cooking-pot": "pho", "sandwich": "banh-mi", "croissant": "banh-mi", "cookie": "candy", "candy": "candy", "nut": "candy", "citrus": "apple", "banana": "apple", "cherry": "apple", "grape": "apple",
    "martini": "wine", "glass-water": "cup-soda", "milk": "cup-soda", "sparkling-wine": "wine", "beef": "chef", "drumstick": "chef", "wheat": "rice", "popcorn": "candy", "refrigerator": "store", "microwave": "store", "oven": "store",
    "chef-hat": "chef", "concierge-bell": "chef", "shopping-cart": "cart", "shopping-basket": "basket", "scale": "calculator",
    // di chuyển
    "car-front": "car", "car-taxi-front": "taxi", "cable-car": "train", "tram-front": "train", "rail-symbol": "train", "caravan": "bus", "truck": "bus", "sailboat": "ship", "anchor": "ship", "ferry": "ship",
    "helicopter": "plane", "rocket": "plane", "plane-takeoff": "plane", "plane-landing": "plane", "map": "map-pin", "navigation": "compass", "navigation-2": "compass", "milestone": "road", "route": "road", "signpost": "road",
    "traffic-cone": "road", "gauge": "scooter", "circle-parking": "parking", "tickets": "ticket", "footprints": "shoe", "battery-charging": "plug", "hammer": "wrench", "plug-zap": "plug", "flashlight": "bulb", "siren": "alert", "circle-dot": "target",
    // nhà
    "building-2": "building", "warehouse": "building", "factory": "building", "droplets": "droplet", "lightbulb": "bulb", "door-closed": "home", "door-open": "home", "fence": "home",
    "air-vent": "fan", "thermometer": "droplet", "wind": "fan", "shower-head": "bath", "waves": "droplet", "paint-bucket": "palette", "brush": "palette", "roller-coaster": "party", "shovel": "sprout",
    "axe": "wrench", "drill": "wrench", "tree-pine": "tree", "trees": "tree", "flower-2": "flower", "plug-2": "plug",
    // mua sắm
    "shopping-bag": "bag", "package-check": "package", "package-open": "package", "package-plus": "package", "boxes": "package", "box": "package", "archive": "package", "hanger": "shirt", "ribbon": "gift",
    "badge": "star", "award": "star", "trophy": "crown", "medal": "star", "smile": "heart", "sun-medium": "sun", "layers": "package",
    // công nghệ
    "smartphone": "phone", "smartphone-charging": "phone", "laptop-2": "laptop", "tablet": "phone", "monitor-speaker": "monitor", "monitor-smartphone": "monitor", "cpu": "laptop", "hard-drive": "laptop",
    "battery": "plug", "battery-full": "plug", "battery-low": "plug", "battery-warning": "plug", "server": "monitor", "database": "monitor", "router": "wifi", "bluetooth": "wifi", "mic": "radio", "mic-2": "radio",
    "video": "camera", "webcam": "camera", "cast": "tv", "mouse": "laptop", "keyboard": "laptop", "usb": "plug", "gamepad-2": "gamepad", "code": "laptop", "terminal": "laptop", "binary": "laptop",
    "cloud-download": "download", "cloud-upload": "cloud-up", "satellite": "radio", "signal": "wifi", "fingerprint": "lock", "bot": "flower",
    // sức khỏe
    "bandage": "cross", "dna": "heart-pulse", "brain": "bulb", "ear": "heart", "accessibility": "yoga", "heart-handshake": "handshake", "bike": "bike", "swords": "dumbbell", "smile-plus": "heart",
    // giải trí
    "music-2": "music", "music-3": "music", "music-4": "music", "clapperboard": "film", "party-popper": "party", "palmtree": "palm", "paw-print": "paw", "dices": "dice", "disc": "vinyl",
    "ferris-wheel": "party", "binoculars": "compass", "drum": "music", "piano": "music",
    // học tập
    "graduation-cap": "graduation", "folder": "file", "folder-open": "file", "presentation": "monitor", "projector": "monitor", "university": "school", "ruler": "pencil", "stamp": "star",
    "paperclip": "file", "bookmark": "book", "microscope": "bulb", "test-tube": "bulb", "flask-conical": "bulb", "atom": "bulb", "languages": "globe",
    // xã hội
    "phone-call": "phone-call", "send": "mail", "share-2": "swap", "church": "pagoda", "help-circle": "info", "laugh": "heart", "inbox": "inbox", "mail": "mail",
    // khác
    "upload-cloud": "cloud-up", "download-cloud": "download", "file-down": "download", "file-up": "upload", "sliders": "settings", "link": "swap", "copy": "file", "clipboard-paste": "file",
    "banknote": "banknote", "coins": "coins", "landmark": "landmark", "receipt": "receipt", "calculator": "calculator", "briefcase": "briefcase", "hand-coins": "hand-coins", "safe": "safe", "handshake": "handshake",
    "gift": "gift", "gem": "gem", "crown": "crown", "star": "star", "heart": "heart", "zap": "zap", "power": "power", "target": "target", "globe": "globe", "camera": "camera", "radio": "radio", "tv": "tv",
    "check": "check", "x": "x", "info": "info", "lock": "lock", "key": "key", "bell": "bell", "user": "user", "users": "users", "flag": "flag", "tag": "tag", "tags": "tags", "home": "home"
  };

  function resolve(name) {
    if (!name) return "tag";
    if (I[name]) return name;
    if (A[name] && I[A[name]]) return A[name];
    return "tag";
  }

  // ---------- Màu (bộ icon nhiều màu kiểu in lụa thập niên 70) ----------
  // Bảng màu: mỗi mã -> biến CSS (theme có thể chỉnh) + màu dự phòng
  var P = {
    mu: "#E9A23B", ye: "#F4CF5D", or: "#E0702F", tm: "#C9452F", ro: "#E28B84", pk: "#F3BDB2",
    av: "#A3AD45", gr: "#5E9150", te: "#2E8C86", mt: "#8CCBB9", sk: "#82B9D4", dn: "#3F6E99",
    cr: "#F7E8C8", wh: "#FFF8EA", br: "#9A6240", cf: "#5C3A26", pl: "#946096", gy: "#BDB3A2",
    ink: "currentColor"
  };
  function col(t) { return t === "ink" ? "currentColor" : "var(--ic-" + t + "," + (P[t] || P.mu) + ")"; }

  // Màu cho từng vùng tô (theo thứ tự các phần tử class f / d / s trong icon)
  var C = {
    "home": "or", "chart": "te ro", "pie": "mu", "trend-up": "av", "tags": "mu wh", "wallet": "or cf", "settings": "mu",
    "palette": "cr tm te dn", "sun": "ye", "moon": "ye", "cloud": "sk", "cloud-up": "sk", "zap": "ye", "bell": "mu",
    "chat": "sk", "sparkles": "ye", "trash": "ro", "pencil": "ye", "search": "sk", "filter": "te", "calendar": "cr tm te",
    "clock": "cr", "info": "sk ink", "alert": "ye ink", "alert-circle": "ro ink", "check-circle": "av", "landmark": "tm",
    "banknote": "av", "credit-card": "dn", "piggy": "pk cf", "coins": "ye mu", "receipt": "cr", "calculator": "te mu mu mu or or or",
    "percent": "ro te", "download": "sk", "upload": "sk", "file": "cr", "user": "mu te", "users": "or te", "mail": "cr",
    "lock": "mu cf", "key": "ye", "eye": "wh", "inbox": "sk", "grid": "or te", "list": "or te mu", "scan": "sk", "camera": "dn",
    "more": "or mu te", "target": "wh tm", "book": "te", "book-open": "cr", "contact": "cr", "save": "dn", "shield": "te",
    "star": "ye", "heart": "tm", "image": "sk ye", "flower": "pk ro pk ro pk ye", "globe": "sk",
    // ăn uống
    "coffee": "cr", "phin": "gy cf", "milk-tea": "pk cf cf cf", "utensils": "gy", "pho": "cr", "rice": "dn wh", "banh-mi": "mu",
    "cup-soda": "tm", "beer": "ye", "wine": "pl", "cake": "pk mu", "ice-cream": "pk", "pizza": "ye tm tm tm", "apple": "tm",
    "cart": "mu ink ink", "basket": "mu", "fish": "sk cf", "egg": "wh", "carrot": "or", "candy": "ro", "chef": "wh",
    // di chuyển
    "scooter": "cf cf tm", "bike": "cf cf", "car": "tm ye ye", "taxi": "ye wh wh", "bus": "or ye ye", "train": "te ye ye",
    "plane": "sk", "ship": "tm", "fuel": "tm", "parking": "dn", "wrench": "gy", "map-pin": "tm", "compass": "cr", "luggage": "or",
    "ticket": "or", "road": "gy",
    // nhà cửa
    "building": "mu", "droplet": "sk", "wifi": "te", "tv": "cf", "bulb": "ye", "bed": "dn", "bath": "wh", "sofa": "te or",
    "fan": "cf sk", "plug": "gy", "flame": "or", "tree": "gr", "sprout": "av", "bag": "or", "shirt": "te", "ao-dai": "tm",
    "shoe": "tm", "watch": "cr", "glasses": "sk sk", "gem": "mt", "gift": "ro", "scissors": "tm tm", "package": "mu", "lipstick": "cf",
    "store": "cr", "crown": "ye", "umbrella": "tm", "non-la": "ye",
    // công nghệ
    "phone": "cf ink", "laptop": "sk", "headphones": "or or", "gamepad": "pl ye ro", "monitor": "sk", "printer": "gy", "radio": "or", "cassette": "mu",
    // tài chính
    "briefcase": "br", "hand-coins": "ye", "safe": "gy", "handshake": "mu", "salary": "av",
    // sức khỏe
    "heart-pulse": "tm", "pill": "wh", "stethoscope": "sk", "dumbbell": "tm tm", "syringe": "sk", "tooth": "wh", "baby": "pk cf cf",
    "cross": "tm", "yoga": "or",
    // giải trí
    "film": "cf", "music": "or te", "guitar": "or", "party": "ye", "paw": "br br br br br", "cat": "mu cf cf", "dog": "br cf cf",
    "palm": "gr", "tent": "or", "mountain": "te", "dice": "wh tm tm tm tm tm", "vinyl": "cf", "disco": "gy",
    // học tập & gia đình
    "graduation": "cf", "school": "tm", "lamp": "mu", "phone-call": "av", "envelope-red": "tm", "pagoda": "tm tm", "flag": "tm",
    "home-heart": "or tm"
  };

  // Chi tiết màu thêm (vẽ dưới nét viền, trên lớp tô chính) – làm icon có nhiều mảng màu
  var X = {
    "home": [["mu", '<path d="M10 20v-5.2a2 2 0 0 1 4 0V20z"/>']],
    "chart": [["mu", '<rect x="10.2" y="5" width="3.6" height="15.5" rx="1.2"/>']],
    "pie": [["te", '<path d="M10 5.7A8.5 8.5 0 1 0 18.3 14H10z"/>']],
    "wallet": [["cr", '<path d="M3.5 6.5A2 2 0 0 1 5.5 4.5H16v3H4z"/>'], ["mu", '<path d="M20.5 11.5h-4a2 2 0 0 0 0 4h4z"/>']],
    "settings": [["cr", '<circle cx="12" cy="12" r="2.8"/>']],
    "sun": [["or", '<circle cx="12" cy="12" r="2.2"/>']],
    "cloud-up": [["wh", '<path d="M12 16v-5.5l-2.5 2.5h5L12 10.5z"/>']],
    "bell": [["or", '<circle cx="12" cy="20.6" r="1.7"/>']],
    "chat": [["wh", '<path d="M8 8.2h8v1.6H8zM8 11.7h5v1.6H8z"/>']],
    "sparkles": [["or", '<path d="M17.5 14l.8 2.2 2.2.8-2.2.8-.8 2.2-.8-2.2-2.2-.8 2.2-.8z"/>']],
    "trash": [["tm", '<path d="M4 6.5h16M9.5 6.5V4.5h5v2"/>']],
    "pencil": [["ro", '<path d="M15.5 4.5l4 4-2 2-4-4z"/>'], ["cr", '<path d="M5 15l4 4-5 1z"/>']],
    "search": [["wh", '<circle cx="9" cy="9" r="1.8"/>']],
    "calendar": [["tm", '<path d="M3.5 7a2 2 0 0 1 2-2h13a2 2 0 0 1 2 2v3h-17z"/>']],
    "clock": [["mu", '<path d="M12 3.5a8.5 8.5 0 0 1 8.5 8.5H12z"/>']],
    "landmark": [["cr", '<path d="M4.5 17.5h15v3h-15z"/>']],
    "banknote": [["ye", '<circle cx="12" cy="12" r="2.6"/>']],
    "credit-card": [["ye", '<path d="M2.5 9.5h19V7a2 2 0 0 0-2-2h-15a2 2 0 0 0-2 2z"/>'], ["mu", '<rect x="5" y="12.5" width="4" height="3" rx=".6"/>']],
    "piggy": [["ro", '<path d="M3.5 11.5H5"/>']],
    "receipt": [["av", '<path d="M8 7.2h8v1.6H8z"/>']],
    "calculator": [["sk", '<rect x="8" y="6" width="8" height="3.5" rx=".6"/>']],
    "download": [["dn", '<path d="M12 3.5v11l-4.5-4.5h9L12 14.5z"/>']],
    "upload": [["dn", '<path d="M12 14.5v-11L7.5 8h9L12 3.5z"/>']],
    "file": [["mu", '<path d="M14 3v5h5z"/>']],
    "mail": [["or", '<path d="M3.5 7l8.5 6 8.5-6V6.5a1 1 0 0 0-1-1h-15a1 1 0 0 0-1 1z"/>']],
    "lock": [["gy", '<path d="M8 10.5V7.5a4 4 0 0 1 8 0v3h-1.6v-3a2.4 2.4 0 0 0-4.8 0v3z"/>']],
    "key": [["or", '<circle cx="7" cy="16" r="1.5"/>']],
    "eye": [["te", '<circle cx="12" cy="12" r="3"/>'], ["cf", '<circle cx="12" cy="12" r="1.3"/>']],
    "inbox": [["mu", '<path d="M3.5 13.5H8l1.5 2.5h5l1.5-2.5h4.5V19a1.5 1.5 0 0 1-1.5 1.5H5A1.5 1.5 0 0 1 3.5 19z"/>']],
    "grid": [["mu", '<rect x="13.5" y="4" width="6.5" height="6.5" rx="1.5"/>'], ["ro", '<rect x="4" y="13.5" width="6.5" height="6.5" rx="1.5"/>']],
    "scan": [["wh", '<path d="M9.5 10.2h5v1.6h-5z"/>']],
    "camera": [["sk", '<circle cx="12" cy="13.2" r="3.6"/>'], ["mu", '<path d="M9 4.5h6L16.5 7h-9z"/>']],
    "target": [["ro", '<circle cx="12" cy="12" r="5"/>'], ["wh", '<circle cx="12" cy="12" r="3"/>']],
    "book": [["cr", '<path d="M4 19.8A1.8 1.8 0 0 0 5.8 21.5H19V18H5.8z"/>'], ["mu", '<path d="M8.5 6.5h6v2h-6z"/>']],
    "book-open": [["or", '<path d="M12 6.5C10 5 7 4.5 3 4.8V18.5c4-.3 7 .2 9 1.7z"/>']],
    "contact": [["mu", '<circle cx="12" cy="10" r="2.8"/>'], ["te", '<path d="M8 17a4 4 0 0 1 8 0z"/>']],
    "save": [["wh", '<path d="M7.5 3.5v5h7v-5z"/>'], ["sk", '<path d="M7.5 20.5v-6h9v6z"/>']],
    "shield": [["mt", '<path d="M12 3 4.5 6v6c0 4.5 3.2 7.8 7.5 9z"/>']],
    "star": [["or", '<path d="M12 3.5v13.3l-5.2 2.8 1-5.8-4.3-4.1 5.9-.8z"/>']],
    "heart": [["ro", '<path d="M7 7.2c-1.6.5-2.5 1.8-2.4 3.3"/>']],
    "image": [["av", '<path d="m3.5 17 5-5 4 4 2.5-2.5 5.5 5V18a1.5 1.5 0 0 1-1.5 1.5h-14A1.5 1.5 0 0 1 3.5 18z"/>']],
    "globe": [["av", '<path d="M7 7c2 0 3 1.5 2.5 3S7 12 6 14c-1 1.5-2 1-2.4-.5A8.5 8.5 0 0 1 7 7zM15 15c1.5-1 3-.5 4 .5a8.5 8.5 0 0 1-4 4c-.8-1.5-1-3.5 0-4.5z"/>']],
    // ăn uống
    "coffee": [["cf", '<path d="M4.5 9.5h12v2h-12z"/>'], ["mu", '<path d="M3.5 21h15v-1h-15z"/>']],
    "phin": [["cf", '<path d="M7.6 17h8.8l-.9 3.5h-7z"/>']],
    "milk-tea": [["wh", '<path d="M8 8.5a4 4 0 0 1 8 0z"/>'], ["tm", '<path d="M13.5 2.5 12 8.5"/>']],
    "utensils": [["br", '<path d="M17.5 13h-2.5v-3c0-3 .5-5.5 2.5-7z"/>']],
    "pho": [["or", '<path d="M4.2 13.5h15.6a8.3 8.3 0 0 1-1.3 2.5H5.5a8.3 8.3 0 0 1-1.3-2.5z"/>'], ["gr", '<circle cx="9" cy="11" r="1.3"/><circle cx="14.5" cy="11.2" r="1.1"/>'], ["br", '<path d="M15 3.5 9 11M19.5 4.5 11 11"/>']],
    "rice": [["sk", '<path d="M5.5 15h13"/>']],
    "banh-mi": [["or", '<path d="M5 15.2c1.8 1 4.6.8 7.6-.6s5.6-3.4 6.6-5.4c-2.2 1.5-5 3.1-8 4-2.6.7-4.6.9-6.2 2z"/>'], ["gr", '<path d="M8.5 13.2c1.5-.2 3.5-.8 5.5-1.8 1.4-.7 2.5-1.4 3.5-2.2"/>']],
    "cup-soda": [["wh", '<path d="M7 13h10l-.3 2.5H7.3z"/>'], ["ye", '<path d="M12 8l1.5-5.5 3 .5"/>']],
    "beer": [["wh", '<path d="M5.5 7.5a2.3 2.3 0 0 1 1.7-3.9 2.8 2.8 0 0 1 5 0 2.3 2.3 0 0 1 3.3 3.9z"/>']],
    "wine": [["tm", '<path d="M7.3 8h9.4a4.7 4.7 0 0 1-9.4 0z"/>']],
    "cake": [["wh", '<path d="M4 12.5h16v3c-1.3 1-2.7 1-4 0s-2.7-1-4 0-2.7 1-4 0-2.7-1-4 0z"/>'], ["sk", '<path d="M11.2 9h1.6v3.5h-1.6zM7.2 10h1.6v2.5H7.2zM15.2 10h1.6v2.5h-1.6z"/>']],
    "ice-cream": [["mu", '<path d="M7 11l5 10.5L17 11z"/>'], ["mt", '<path d="M7 11a5 5 0 0 1 5-5v5z"/>']],
    "pizza": [["mu", '<path d="M3.5 6.5C9 3.5 15 3.5 20.5 6.5l-1.3 3c-4.6-2.2-9.8-2.2-14.4 0z"/>'], ["gr", '<circle cx="12.5" cy="10.6" r=".8"/><circle cx="15.5" cy="11.2" r=".7"/>']],
    "apple": [["gr", '<path d="M14.5 3.5c-1.7.5-2.5 2-2.5 4 1.5-.3 2.5-1.8 2.5-4z"/>'], ["ro", '<path d="M7.5 10c.4-1.3 1.3-2 2.3-2.2"/>']],
    "cart": [["dn", '<path d="M6.3 7h14.2l-1 4H7.1z"/>']],
    "basket": [["br", '<path d="M3.5 10h17l-.4 2H3.9z"/>'], ["av", '<circle cx="9.5" cy="8.3" r="2"/>'], ["tm", '<circle cx="14" cy="8" r="2"/>']],
    "fish": [["dn", '<path d="M21 6v12l-4.5-3.7V9.7z"/>']],
    "egg": [["ye", '<circle cx="12" cy="14" r="3"/>']],
    "carrot": [["gr", '<path d="M14.5 9.5c.5-2.5 2-4 4-4.5M15.5 10.5c2.3-.6 4-.2 5 1M15 10c0-2.5.8-4.5 2-6"/>']],
    "candy": [["ye", '<path d="M8.5 9.8 3.5 8l1 4 4.1.3zM15.5 14.2l5 1.8-1-4-4.1-.3z"/>'], ["wh", '<path d="M10 10.5c1.5-1 3-1 4 .5"/>']],
    "chef": [["tm", '<path d="M7 15.5h10v1.8H7z"/>']],
    // di chuyển
    "scooter": [["sk", '<path d="M13.5 11.5 16 6h2.5"/>'], ["gy", '<circle cx="5.5" cy="17" r="1"/><circle cx="18.5" cy="17" r="1"/>']],
    "bike": [["gy", '<circle cx="5.5" cy="16.5" r="1.3"/><circle cx="18.5" cy="16.5" r="1.3"/>'], ["te", '<path d="M5.5 16.5 9.5 9h6l3 7.5"/>']],
    "car": [["sk", '<path d="M5.7 12.5l1.6-4.3a1 1 0 0 1 .9-.7h7.6a1 1 0 0 1 .9.7l1.6 4.3z"/>'], ["cf", '<rect x="5.5" y="17" width="2" height="2.5" rx=".5"/><rect x="16.5" y="17" width="2" height="2.5" rx=".5"/>']],
    "taxi": [["sk", '<path d="M5.7 12.5l1.6-4.3a1 1 0 0 1 .9-.7h7.6a1 1 0 0 1 .9.7l1.6 4.3z"/>'], ["tm", '<path d="M10 3.5h4v2.5h-4z"/>']],
    "bus": [["sk", '<path d="M4.5 6a2.5 2.5 0 0 1 2.5-2.5h10A2.5 2.5 0 0 1 19.5 6v5h-15z"/>'], ["cr", '<path d="M8 6.2h8v1.6H8z"/>']],
    "train": [["sk", '<path d="M5.5 6a3 3 0 0 1 3-3h7a3 3 0 0 1 3 3v4h-13z"/>']],
    "plane": [["wh", '<path d="M14.8 8 5.5 5.5 3.8 7.2l7.4 4.3z"/>'], ["dn", '<path d="M16.2 9.2l3.8 6.6-1.7 1.7-4.3-7.4z"/>']],
    "ship": [["wh", '<path d="M5.5 14.5v-5h13v5z"/>'], ["mu", '<path d="M9 9.5V5.5h6v4z"/>'], ["dn", '<path d="M3 14.5h18l-.8 1.6H3.8z"/>']],
    "fuel": [["wh", '<rect x="7" y="6.5" width="5" height="3" rx=".5"/>']],
    "parking": [["wh", '<path d="M9.5 17V7.5h3.3a2.7 2.7 0 0 1 0 5.5H9.5"/>']],
    "wrench": [["or", '<circle cx="5" cy="17.8" r="1"/>']],
    "map-pin": [["wh", '<circle cx="12" cy="10" r="2.4"/>']],
    "compass": [["tm", '<path d="m15.5 8.5-2 5-3-3z"/>'], ["dn", '<path d="m8.5 15.5 2-5 3 3z"/>']],
    "luggage": [["mu", '<path d="M9.5 10.5h5v6h-5z"/>'], ["cf", '<path d="M9.5 7V4.5h5V7"/>']],
    "ticket": [["ye", '<path d="M14.5 5.5h4.5A1.5 1.5 0 0 1 20.5 7v2.5a2.5 2.5 0 0 0 0 5V17a1.5 1.5 0 0 1-1.5 1.5h-4.5z"/>']],
    "road": [["ye", '<path d="M11.3 5.5h1.4v2.5h-1.4zM11.3 11h1.4v2.5h-1.4zM11.3 16.5h1.4v3h-1.4z"/>'], ["gr", '<path d="M3.5 20.5 7 3.5h1l-3.5 17zM20.5 20.5 17 3.5h-1l3.5 17z"/>']],
    // nhà cửa
    "building": [["gy", '<path d="M15 9.5h3.5A1.5 1.5 0 0 1 20 11v9.5h-5z"/>'], ["sk", '<path d="M8.5 6.7h2.5v1.6H8.5zM8.5 10.2h2.5v1.6H8.5zM8.5 13.7h2.5v1.6H8.5z"/>']],
    "droplet": [["wh", '<path d="M9 14.5a3 3 0 0 0 3 3"/>']],
    "tv": [["sk", '<rect x="5" y="8.5" width="14" height="8" rx="1"/>'], ["or", '<path d="m8.5 3 3.5 3.5L15.5 3"/>']],
    "bulb": [["gy", '<path d="M9 16.5h6v3.5H9z"/>'], ["wh", '<path d="M8.5 9a3.5 3.5 0 0 1 3-3.4"/>']],
    "bed": [["wh", '<circle cx="6.8" cy="11.2" r="1.6"/>'], ["mu", '<path d="M3 17h18v-1.5H3z"/>'], ["br", '<path d="M3 5v15M21 13v7"/>']],
    "bath": [["sk", '<path d="M3 12h18v1.5H3z"/>'], ["gy", '<path d="M6 12V5.5A2 2 0 0 1 8 3.5a2 2 0 0 1 2 2"/>']],
    "sofa": [["mu", '<path d="M7 14.5h10V13H7z"/>']],
    "fan": [["gy", '<path d="M12 16.5v4M8.5 20.5h7"/>']],
    "plug": [["ye", '<path d="M9.5 8.5V3.5M14.5 8.5V3.5"/>'], ["cf", '<path d="M12 16.5v5"/>']],
    "flame": [["ye", '<path d="M12 21.5a2.5 2.5 0 0 1-2.5-2.5c0-1.8 2.5-3.5 2.5-3.5s2.5 1.7 2.5 3.5a2.5 2.5 0 0 1-2.5 2.5z"/>']],
    "tree": [["av", '<path d="M12 2.5 8.5 7.2h7z"/>'], ["br", '<path d="M12 16v5.5"/>']],
    "sprout": [["gr", '<path d="M12 10.5c0-3.5 2.7-6 7.5-6 0 4.2-2.7 6.5-7.5 6.5z"/>'], ["br", '<path d="M8 21h8"/>']],
    "bag": [["mu", '<path d="M5.2 10h13.6l.3 3.5H4.9z"/>']],
    "shirt": [["wh", '<path d="M8.5 3.5c.5 1.5 2 2.5 3.5 2.5s3-1 3.5-2.5L14 2.8c-.5 1-1.2 1.4-2 1.4s-1.5-.4-2-1.4z"/>'], ["mu", '<path d="M3 6.5l2 4 1.5-.8-2-4zM21 6.5l-2 4-1.5-.8 2-4z"/>']],
    "ao-dai": [["ye", '<path d="M9.5 3.5h5l.6.8L12 6.5 8.9 4.3z"/>'], ["pk", '<path d="M10 12h4l1.9 8H8.1z"/>']],
    "shoe": [["wh", '<path d="M3 16.5h18v2H3z"/>'], ["ye", '<path d="M9 9.5l1.5-1.5M11 11l1.5-1.5"/>']],
    "watch": [["br", '<path d="M9 7l.7-3.5h4.6L15 7zM9 17l.7 3.5h4.6L15 17z"/>']],
    "glasses": [["cf", '<path d="M10 14.5c1.3-1 2.7-1 4 0"/>'], ["wh", '<path d="M4.5 13.5a2 2 0 0 1 2-2M15.5 13.5a2 2 0 0 1 2-2"/>']],
    "gem": [["sk", '<path d="M3 9h18l-9 11.5z"/>'], ["wh", '<path d="M9.5 4 8 9h8l-1.5-5z"/>']],
    "gift": [["ye", '<path d="M3 7h18v3H3z"/>'], ["mu", '<path d="M11 7h2v13.5h-2z"/>'], ["tm", '<path d="M12 7c-1-3.5-5.5-4-5.5-1.5S12 7 12 7zm0 0c1-3.5 5.5-4 5.5-1.5S12 7 12 7z"/>']],
    "scissors": [["gy", '<path d="M9 8.5 20 18M9 15.5 20 6"/>']],
    "package": [["br", '<path d="M12 11 20 7v10l-8 4z"/>'], ["cr", '<path d="M8 5l8 4-1.5.8-8-4z"/>']],
    "lipstick": [["tm", '<path d="M10 11V6.5l4-3V11z"/>'], ["mu", '<path d="M9 11h6v2H9z"/>']],
    "store": [["tm", '<path d="M3 9.5 5 4h14l2 5.5a2.5 2.5 0 0 1-4.5 1.5 2.5 2.5 0 0 1-4.5 0 2.5 2.5 0 0 1-4.5 0A2.5 2.5 0 0 1 3 9.5z"/>'], ["wh", '<path d="M7.5 4h3l-.8 7a2.5 2.5 0 0 1-2.2 0zM13.5 4h3l.3 7a2.5 2.5 0 0 1-2.3 0z"/>'], ["sk", '<path d="M9.5 20.5v-5h5v5z"/>']],
    "crown": [["tm", '<circle cx="12" cy="13.5" r="1.4"/>'], ["te", '<circle cx="8" cy="15" r="1"/><circle cx="16" cy="15" r="1"/>'], ["mu", '<path d="M5.2 18h13.6l-.3 2.5h-13z"/>']],
    "umbrella": [["wh", '<path d="M12 3a9 9 0 0 0-3 .5c-1.6 2.3-2.3 4.8-2.5 8 1.5-1.5 3-1.5 4.5 0 .3-3 .7-5.7 1-8.5zM12 3a9 9 0 0 1 3 .5c1.6 2.3 2.3 4.8 2.5 8-1.5-1.5-3-1.5-4.5 0-.3-3-.7-5.7-1-8.5z"/>'], ["br", '<path d="M12 12v6.5a2 2 0 0 1-4 0"/>']],
    "non-la": [["mu", '<path d="M7 17.2 12 4 9.5 17.4z"/>'], ["tm", '<path d="M2.5 15.5h19l-1.5.8H4z"/>']],
    // công nghệ
    "phone": [["sk", '<rect x="7.8" y="7" width="8.4" height="9" rx=".8"/>'], ["wh", '<circle cx="12" cy="18" r="1.1"/>']],
    "laptop": [["dn", '<rect x="6.5" y="6.5" width="11" height="6.5" rx=".6"/>'], ["gy", '<path d="M2.5 18.5h19l-1.5-3.5H4z"/>']],
    "headphones": [["te", '<path d="M4 15v-3a8 8 0 0 1 16 0v3"/>'], ["cf", '<rect x="5" y="15" width="1.2" height="4" rx=".6"/><rect x="17.8" y="15" width="1.2" height="4" rx=".6"/>']],
    "gamepad": [["te", '<path d="M7.5 10v4M5.5 12h4"/>']],
    "monitor": [["dn", '<rect x="5" y="6" width="14" height="8.5" rx="1"/>'], ["gy", '<path d="M12 16.5v4M8 20.5h8"/>'], ["mu", '<circle cx="9" cy="9" r="1.4"/>']],
    "printer": [["wh", '<path d="M7 9V3.5h10V9zM7 14h10v7H7z"/>'], ["av", '<circle cx="17.5" cy="12" r=".9"/>']],
    "radio": [["cf", '<circle cx="15.5" cy="14.2" r="3"/>'], ["cr", '<path d="M6.5 11.2h4v1.6h-4zM6.5 14.2h4v1.6h-4z"/>']],
    "cassette": [["cf", '<rect x="5" y="8" width="14" height="6" rx="1"/>'], ["wh", '<circle cx="8" cy="11" r="2"/><circle cx="16" cy="11" r="2"/>'], ["or", '<path d="M6.5 18.5l1.5-3h8l1.5 3z"/>']],
    // tài chính
    "briefcase": [["mu", '<path d="M10.5 12.5v2h3v-2z"/>'], ["cf", '<path d="M3 12.5h18v1H3z"/>']],
    "hand-coins": [["pk", '<path d="M2.5 13.5h3l3.5-1.5h4a1.5 1.5 0 0 1 0 3H10m3.5 0 5-2.5a1.6 1.6 0 0 1 1.8 2.6L15 19.5H5.5L2.5 21z"/>'], ["mu", '<circle cx="15.5" cy="6.5" r="1.6"/>']],
    "safe": [["mu", '<circle cx="12" cy="11" r="3.5"/>'], ["cf", '<path d="M6.5 18.5v2M17.5 18.5v2"/>']],
    "handshake": [["pk", '<path d="m11.5 8-3 3a1.4 1.4 0 0 0 2 2l2-2 3 3.5-2.5 3c-.8.8-1.8.8-2.5 0L3 11l4.5-5z"/>']],
    "salary": [["ye", '<circle cx="12" cy="12" r="3.6"/>'], ["gr", '<path d="M3 9a2.5 2.5 0 0 0 2.5-2.5M21 9a2.5 2.5 0 0 1-2.5-2.5M3 15a2.5 2.5 0 0 1 2.5 2.5M21 15a2.5 2.5 0 0 0-2.5 2.5"/>']],
    // sức khỏe
    "heart-pulse": [["wh", '<path d="M5 12.5h3.5l1.5-3 2.5 5.5 1.5-2.5H19"/>']],
    "pill": [["tm", '<path d="m4.9 12.4 3.8-3.7 6.6 6.6-3.8 3.7a4.6 4.6 0 0 1-6.6-6.6z"/>']],
    "stethoscope": [["gy", '<path d="M5.5 3.5H4v5a5 5 0 0 0 10 0v-5h-1.5"/>'], ["te", '<circle cx="19" cy="11" r="1"/>']],
    "dumbbell": [["cf", '<rect x="5.5" y="7" width="3" height="10" rx="1"/><rect x="15.5" y="7" width="3" height="10" rx="1"/>'], ["gy", '<path d="M8.5 12h7"/>']],
    "syringe": [["tm", '<path d="m12.8 6.2 5 5-4.2 4.2-5-5z"/>'], ["gy", '<path d="m17 3 4 4M5.5 13.5 3 21"/>']],
    "tooth": [["sk", '<path d="M7 7.5c0-1.2.8-2 2-2.2"/>']],
    "baby": [["ro", '<circle cx="7.6" cy="14" r="1.2"/><circle cx="16.4" cy="14" r="1.2"/>'], ["mu", '<path d="M12 4.5c-1.5 0-2 2 0 2.5"/>']],
    "cross": [["wh", '<path d="M11 6h2v12h-2zM6 11h12v2H6z"/>']],
    "yoga": [["te", '<path d="M12 7.5v6M5 10l7 1.5 7-1.5"/>'], ["mu", '<path d="M4 20.5h16v-1H4z"/>']],
    // giải trí
    "film": [["mu", '<path d="M7.5 3.5h9v8.5h-9z"/>'], ["or", '<path d="M7.5 12h9v8.5h-9z"/>']],
    "music": [["cf", '<path d="M9 17.5V5.5l11-2v4l-11 2z"/>']],
    "guitar": [["cf", '<circle cx="8.5" cy="15.5" r="1.6"/>'], ["br", '<path d="m12 12.5 8-8M18 3l3 3"/>']],
    "party": [["tm", '<path d="M5.6 14.5l3.9 3.9-3.2 1.1-1.5-1.5z"/>'], ["te", '<path d="M7.1 10.2l6.7 6.7-2.4.8-5-5z"/>'], ["pl", '<path d="M13 3.5c.5 1.5 0 3-1.5 3.5M20.5 11c-1.5-.5-3 0-3.5 1.5"/>']],
    "paw": [["pk", '<path d="M10.5 15.5h3l-1.5 1.8z"/>']],
    "cat": [["pk", '<path d="M5.8 6.2V5.6l1.7 1.3zM18.2 6.2V5.6l-1.7 1.3zM11 15.5h2l-1 1z"/>'], ["or", '<path d="M10.5 7h3l-1.5 2.5z"/>']],
    "dog": [["cf", '<path d="M7.5 7 4 5c-1 2-.5 5 1.5 6.5zM16.5 7 20 5c1 2 .5 5-1.5 6.5z"/>'], ["cr", '<path d="M9.5 15a2.5 2.5 0 0 0 5 0c0-1-1-1.5-2.5-1.5S9.5 14 9.5 15z"/>']],
    "palm": [["br", '<path d="M12 21.5c0-5 .5-9 2-12.5"/>'], ["av", '<path d="M14 9c-2.5-3-6.5-3.5-9-1.5C8 7 11 7.5 14 9zM14 9c3 0 6 2 7 5-3 0-5.5-1.5-7-5z"/>'], ["mu", '<path d="M8 21.5h8v-1H8z"/>']],
    "tent": [["mu", '<path d="M12 11l-3.5 9h7z"/>'], ["tm", '<path d="M12 4l-1.2-2M12 4l1.2-2"/>']],
    "mountain": [["wh", '<path d="M7.2 11.8 9 8.5l1.5 2.5L9 13z"/>'], ["gr", '<path d="M12.5 14l2.5-3.5 6.5 9.5h-6z"/>'], ["ye", '<circle cx="18" cy="5.5" r="1.8"/>']],
    "vinyl": [["tm", '<circle cx="12" cy="12" r="2.5"/>'], ["gy", '<path d="M12 6.5a5.5 5.5 0 0 0-5.5 5.5M17.5 12a5.5 5.5 0 0 1-5.5 5.5"/>']],
    "disco": [["sk", '<path d="M4.5 13a7.5 7.5 0 0 1 1.5-4.2h6V13zM12 13h7.5a7.5 7.5 0 0 1-1.5 4.2h-6z"/>'], ["pk", '<path d="M12 5.5V8.8h6a7.5 7.5 0 0 0-6-3.3zM12 20.5v-3.3H6a7.5 7.5 0 0 0 6 3.3z"/>'], ["ye", '<path d="M12 2.5v3"/>']],
    // học tập & gia đình
    "graduation": [["mu", '<path d="M21.5 9.5v5.5"/><circle cx="21.5" cy="15.8" r="1.2"/>'], ["dn", '<path d="M6.5 11.5V16c0 1.5 2.5 3 5.5 3s5.5-1.5 5.5-3v-4.5L12 14z"/>']],
    "school": [["cr", '<path d="M10 20.5v-5h4v5z"/>'], ["ye", '<path d="M12 5.5V2.5h3"/><circle cx="12" cy="11" r="1.6"/>']],
    "lamp": [["gy", '<path d="M12 10.5v8M8 20.5h8"/>'], ["ye", '<path d="M8 10.5h8l-1 2H9z"/>']],
    "phone-call": [["or", '<path d="M14.5 3.5a6 6 0 0 1 6 6M14.5 7a2.5 2.5 0 0 1 2.5 2.5"/>']],
    "envelope-red": [["ye", '<circle cx="12" cy="13" r="2.2"/>'], ["mu", '<path d="M5 7.5c2.3 2 4.7 3 7 3s4.7-1 7-3V4.3A1.8 1.8 0 0 0 17.2 2.5H6.8A1.8 1.8 0 0 0 5 4.3z"/>']],
    "pagoda": [["mu", '<path d="M8.5 14.5v6h7v-6zM8 9.5v2h8v-2z"/>'], ["ye", '<path d="M12 2.5v4"/>']],
    "flag": [["ye", '<circle cx="10.5" cy="7.5" r="1.6"/>'], ["br", '<path d="M5.5 21.5V4"/>']],
    "home-heart": [["mu", '<path d="M10 20v-1.5h4V20z"/>']]
  };
  X["bell-ring"] = X["bell"]; C["bell-ring"] = C["bell"]; X["key-house"] = X["key"]; C["key-house"] = C["key"];
  X["trash-can"] = X["trash"]; C["trash-can"] = C["trash"]; C["pen"] = C["pencil"]; X["pen"] = X["pencil"];
  C["tag"] = C["tags"]; C["bolt"] = C["zap"]; C["chart-up"] = C["trend-up"]; C["shield-check"] = C["shield"]; X["shield-check"] = X["shield"];
  C["heart-pulse"] = C["heart"];

  var EL = /<(path|rect|circle|ellipse)\b([^>]*?)\/>/g;
  function paint(el, attrs, fill, stroke) {
    return "<" + el + attrs.replace(/\sclass="\w"/, "") + ' style="fill:' + fill + ";stroke:" + stroke + '"/>';
  }
  var cache = {};
  // mono = true -> bản đơn sắc cũ (dùng khi icon nằm trên nền màu đặc)
  function build(key, mono) {
    var body = I[key], colors = (C[key] || "").split(" ").filter(Boolean), k = 0;
    var under = "", lines = "", m;
    EL.lastIndex = 0;
    var top = body.replace(EL, function (all, el, attrs) {
      var c = (attrs.match(/class="(\w)"/) || [])[1];
      if (!c) return all;
      var tok = colors[k++] || (c === "s" ? "ink" : "mu");
      if (mono) {
        if (c === "s") return paint(el, attrs, "currentColor", "none");
        under += paint(el, attrs, "currentColor;fill-opacity:.2", "none");
        return c === "d" ? "" : paint(el, attrs, "none", "currentColor");
      }
      if (c === "s") return paint(el, attrs, col(tok), "none");
      under += paint(el, attrs, col(tok), "none");
      return c === "d" ? "" : paint(el, attrs, "none", "currentColor");
    });
    if (!mono && X[key]) X[key].forEach(function (a) {
      a[1].replace(EL, function (all, el, attrs) {
        var line = el === "path" && !/z\s*"/i.test(attrs) && !/z/i.test((attrs.match(/\sd="([^"]*)"/) || [])[1] || "");
        if (line) lines += paint(el, attrs, "none", col(a[0]));
        else under += paint(el, attrs, col(a[0]), "none");
      });
    });
    return under + top + lines;
  }
  function svg(name, cls, opts) {
    var key = resolve(name), mono = !!(opts && opts.mono), ck = key + (mono ? "|m" : "");
    if (!cache[ck]) cache[ck] = build(key, mono);
    return '<svg class="vi ' + (cls || "") + '" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">' + cache[ck] + "</svg>";
  }

  // Bộ chọn icon khi tạo hạng mục
  var GROUPS = [
    { name: "Ăn uống", icon: "pho", icons: ["coffee", "phin", "milk-tea", "cup-soda", "utensils", "pho", "rice", "banh-mi", "chef", "pizza", "cake", "ice-cream", "candy", "apple", "fish", "egg", "carrot", "beer", "wine", "cart", "basket"] },
    { name: "Di chuyển", icon: "scooter", icons: ["scooter", "bike", "car", "taxi", "bus", "train", "plane", "ship", "fuel", "parking", "wrench", "road", "map-pin", "compass", "luggage", "ticket"] },
    { name: "Nhà cửa", icon: "home", icons: ["home", "building", "zap", "droplet", "wifi", "tv", "bulb", "bed", "bath", "sofa", "fan", "plug", "flame", "key", "lock", "tree", "sprout", "flower", "trash"] },
    { name: "Mua sắm", icon: "bag", icons: ["bag", "shirt", "ao-dai", "shoe", "watch", "glasses", "gem", "lipstick", "scissors", "gift", "package", "store", "crown", "umbrella", "non-la", "tag", "sparkles"] },
    { name: "Công nghệ", icon: "phone", icons: ["phone", "laptop", "monitor", "headphones", "gamepad", "camera", "printer", "radio", "cassette", "wifi", "plug"] },
    { name: "Tài chính", icon: "coins", icons: ["wallet", "credit-card", "banknote", "coins", "piggy", "salary", "hand-coins", "landmark", "safe", "receipt", "calculator", "percent", "trend-up", "trend-down", "pie", "chart", "briefcase", "handshake", "shield"] },
    { name: "Sức khỏe", icon: "heart", icons: ["heart", "heart-pulse", "pill", "stethoscope", "syringe", "tooth", "cross", "dumbbell", "activity", "yoga", "baby"] },
    { name: "Giải trí", icon: "film", icons: ["film", "music", "guitar", "vinyl", "disco", "party", "dice", "gamepad", "ticket", "palm", "tent", "mountain", "paw", "cat", "dog", "camera"] },
    { name: "Học tập", icon: "book", icons: ["book", "book-open", "graduation", "pencil", "school", "lamp", "file", "calculator", "globe", "bulb"] },
    { name: "Gia đình", icon: "users", icons: ["users", "user", "baby", "home-heart", "envelope-red", "gift", "cake", "phone-call", "mail", "pagoda", "flag", "star", "heart", "handshake", "more"] }
  ];
  var all = [];
  GROUPS.forEach(function (g) { g.icons.forEach(function (n) { if (all.indexOf(n) === -1) all.push(n); }); });
  GROUPS.unshift({ name: "Tất cả", icon: "grid", icons: all });

  root.VinaIcons = { svg: svg, PALETTE: P, COLORS: C, resolve: resolve, has: function (n) { return !!I[n]; }, names: Object.keys(I), GROUPS: GROUPS, ALIASES: A };
})(typeof window !== "undefined" ? window : globalThis);
