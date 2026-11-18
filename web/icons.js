/*
 * Bộ icon VinaSpends "Retro Pixel Pastel" – pixel sprite SVG 24×24 sắc nét, không dùng thư viện ngoài.
 * Lưới 24×24 snap nguyên, shape-rendering="crispEdges", viền mực pixel đậm + mảng màu pastel tương phản cao.
 * Lớp:
 *   f  = tô pastel theo bảng P (map C) + viền mực pixel
 *   d  = chỉ tô mảng phụ, không viền
 *   s  = chấm pixel đặc
 * Dùng: VinaIcons.svg(name) -> chuỗi <svg>; VinaIcons.has(name); VinaIcons.GROUPS cho bộ chọn icon.
 */
(function (root) {
  "use strict";
  var I = {};

  // Helper tạo đường gấp khúc / khối pixel nhanh gọn
  function px(list) {
    return list.map(function (r) {
      return '<rect' + (r[4] ? ' class="' + r[4] + '"' : '') + ' x="' + r[0] + '" y="' + r[1] + '" width="' + r[2] + '" height="' + r[3] + '"/>';
    }).join('');
  }

  // ---------- Giao diện & Điều hướng (Pixel Sprites tinh chỉnh từng ô) ----------
  I["home"] = '<path class="f" d="M11 3h2v2h2v2h2v2h2v2h-2v9h-4v-5h-2v5H7v-9H5V9h2V7h2V5h2z"/>' +
    '<path d="M11 3h2v2h2v2h2v2h2v2h-2v9H7v-9H5V9h2V7h2V5h2V3zM10 20v-5h4v5"/>';
  I["chart"] = '<path d="M3 20h18"/>' +
    '<rect class="f" x="4" y="11" width="4" height="9"/>' +
    '<rect class="f" x="10" y="4" width="4" height="16"/>' +
    '<rect class="f" x="16" y="14" width="4" height="6"/>';
  I["pie"] = '<path class="f" d="M13 3h4v2h2v2h2v4h-8V3z"/>' +
    '<path d="M10 5h1v8h8v2h-2v2h-2v2h-6v-2H7v-2H5V9h2V7h3V5z"/>';
  I["trend-up"] = '<path class="d" d="M4 20v-4l4-4 4 3 7-7v12z"/>' +
    '<path d="M3 17h2v-2h2v-2h2v-2h2v2h2v2h2v-2h2v-2h2V9h1V7M15 7h5v5"/>';
  I["trend-down"] = '<path d="M3 7h2v2h2v2h2v2h2v-2h2V9h2v2h2v2h2v2h1v2M15 17h5v-5"/>';
  I["tags"] = '<path class="f" d="M4 4h7v2h2v2h2v2h2v2h2v2h-2v2h-2v2h-2v-2h-2v-2H9v-2H7v-2H5V9H4V4z"/>' +
    '<rect class="s" x="7" y="7" width="2" height="2"/>';
  I["tag"] = I["tags"];
  I["wallet"] = '<rect class="f" x="3" y="7" width="18" height="13"/>' +
    '<path d="M5 4h11v3H3V6h2V4z"/>' +
    '<rect x="15" y="11" width="6" height="5"/>' +
    '<rect class="s" x="17" y="13" width="2" height="2"/>';
  I["plus"] = '<path d="M11 5h2v6h6v2h-6v6h-2v-6H5v-2h6V5z"/>';
  I["minus"] = '<path d="M5 11h14v2H5z"/>';
  I["x"] = '<path d="M6 6h2v2h2v2h4V8h2V6h2v2h-2v2h-2v4h2v2h2v2h-2v-2h-2v-2h-4v2H8v2H6v-2h2v-2h2v-4H8V8H6z"/>';
  I["check"] = '<path d="M18 6h2v2h-2v2h-2v2h-2v2h-2v2h-2v2H8v-2H6v-2H4v-2h2v2h2v2h2v-2h2v-2h2v-2h2V8h2z"/>';
  I["chevron-left"] = '<path d="M14 5h2v2h-2v2h-2v2h-2v2h2v2h2v2h2v2h-2v-2h-2v-2h-2v-2H8v-2h2V9h2V7h2z"/>';
  I["chevron-right"] = '<path d="M8 5h2v2h2v2h2v2h2v2h-2v2h-2v2h-2v2H8v-2h2v-2h2v-2h2v-2h-2V9h-2V7H8z"/>';
  I["chevron-up"] = '<path d="M11 8h2v2h2v2h2v2h2v2h-2v-2h-2v-2h-2v-2h-2v2H9v2H7v2H5v-2h2v-2h2v-2h2z"/>';
  I["chevron-down"] = '<path d="M5 8h2v2h2v2h2v2h2v-2h2v-2h2V8h2v2h-2v2h-2v2h-2v2h-2v-2H9v-2H7v-2H5z"/>';
  I["arrow-left"] = '<path d="M10 6h2v2h-2v2H8v1h11v2H8v1h2v2h2v2h-2v-2H8v-2H6v-2H5v-2h1V8h2V6z"/>';
  I["arrow-up-right"] = '<path d="M9 5h10v10h-2V9h-2v2h-2v2h-2v2H9v2H7v2H5v-2h2v-2h2v-2h2v-2h2V9h2V7H9z"/>';
  I["arrow-down-left"] = '<path d="M17 5h2v2h-2v2h-2v2h-2v2h-2v2h-2v2h6v2H5V9h2v6h2v-2h2v-2h2V9h2V7h2z"/>';
  I["swap"] = '<path d="M4 8h11V6h2v2h2v2H4V8zM15 12h2v-2h-2v2zM20 14H9v-2H7v2H5v2h15v-2zM7 18h2v-2H7v2z"/>';
  I["settings"] = '<path class="f" d="M10 3h4v2h2V4h2v2h2v2h-1v2h2v4h-2v2h1v2h-2v2h-2v-1h-2v2h-4v-2H8v1H6v-2H4v-2h1v-2H3v-4h2V8H4V6h2V4h2v1h2V3z"/>' +
    '<rect x="9" y="9" width="6" height="6"/>';
  I["palette"] = '<path class="f" d="M8 4h8v2h3v3h1v5h-2v2h-4v-2h-2v3h1v2H7v-2H5v-3H4V9h2V6h2z"/>' +
    '<rect class="s" x="7" y="10" width="2" height="2"/><rect class="s" x="10" y="6" width="2" height="2"/><rect class="s" x="14" y="7" width="2" height="2"/>';
  I["sun"] = '<rect class="f" x="8" y="8" width="8" height="8"/>' +
    '<path d="M11 2h2v4h-2zM11 18h2v4h-2zM2 11h4v2H2zM18 11h4v2h-4zM5 5h2v2H5zM17 5h2v2h-2zM5 17h2v2H5zM17 17h2v2h-2z"/>';
  I["moon"] = '<path class="f" d="M9 4h4v2h-2v2h-1v4h1v2h2v2h3v-2h2v2h-1v2h-3v2H8v-2H6v-2H4V9h2V6h3z"/>';
  I["cloud"] = '<path class="f" d="M9 6h6v2h2v2h3v2h1v4h-2v2H5v-2H3v-4h2v-2h2V8h2z"/>';
  I["cloud-off"] = '<path d="M9 6h6v2h2v2h3v2h1v4h-2v2H5v-2H3v-4h2v-2h2V8h2zM4 4h2v2H4zM6 6h2v2H6zM16 16h2v2h-2zM18 18h2v2h-2z"/>';
  I["cloud-up"] = '<path class="f" d="M9 5h6v2h2v2h3v2h1v4h-2v2H5v-2H3v-4h2v-2h2V7h2z"/>' +
    '<path d="M11 10h2v7h-2zM9 12h2v2H9zM13 12h2v2h-2z"/>';
  I["refresh"] = '<path d="M7 5h10v2h2v2h1v1h-5V8h2V7H7v2H5v2H3V7h2V5zM4 13h2v2h1v2h10v-2h2v-2h2v4h-2v2H7v-2H5v-2H4zM18 5h2v4h-2zM4 15h2v4H4z"/>';
  I["zap"] = '<path class="f" d="M11 3h4v4h-2v3h6v2h-2v2h-2v2h-2v5h-3v-5H8v-2H6v-2H5v-2h3V7h2V5h1z"/>';
  I["bell"] = '<path class="f" d="M10 4h4v2h2v3h1v6h2v2H5v-2h2V9h1V6h2z"/>' +
    '<path d="M10 19h4v2h-4z"/>';
  I["bell-ring"] = I["bell"] + '<path d="M3 7h2v4H3zM19 7h2v4h-2z"/>';
  I["bell-off"] = '<path d="M9 4h5v2h2v3h1v5h-2V9h-1V6h-4V4zM5 15h11v2H5zM10 19h4v2h-4zM4 4h2v2H4zM6 6h2v2H6zM16 16h2v2h-2zM18 18h2v2h-2z"/>';
  I["chat"] = '<path class="f" d="M4 4h16v12h-9l-4 4v-4H4z"/>' +
    '<path d="M8 8h8M8 11h5"/>';
  I["sparkles"] = '<path class="f" d="M9 3h2v4h2v2h4v2h-4v2h-2v4H9v-4H7v-2H3V9h4V7h2z"/>' +
    '<path d="M17 13h2v2h2v2h-2v2h-2v-2h-2v-2h2z"/>';
  I["trash"] = '<path d="M4 6h16M9 4h6v2H9z"/>' +
    '<rect class="f" x="6" y="6" width="12" height="14"/>' +
    '<path d="M10 10v7M14 10v7"/>';
  I["pencil"] = '<path class="f" d="M14 4h2v2h2v2h2v2h-2v2h-2v2h-2v2h-2v2H9v2H4v-5h2v-2h2v-2h2V9h2V7h2z"/>' +
    '<path d="M13 6h2v2h2v2"/>';
  I["search"] = '<rect class="f" x="4" y="4" width="11" height="11"/>' +
    '<path d="M15 15h2v2h2v2h1v1h-2v-1h-2v-2h-1z"/>';
  I["filter"] = '<path class="f" d="M4 4h16v3h-2v2h-2v2h-2v8l-4 2v-10H8V9H6V7H4z"/>';
  I["calendar"] = '<rect class="f" x="4" y="5" width="16" height="15"/>' +
    '<path d="M4 10h16M7 3v4M17 3v4"/>' +
    '<rect class="s" x="7" y="13" width="2" height="2"/><rect class="s" x="11" y="13" width="2" height="2"/><rect class="s" x="15" y="13" width="2" height="2"/>';
  I["clock"] = '<rect class="f" x="4" y="4" width="16" height="16"/>' +
    '<path d="M11 7h2v5h4v2h-6z"/>';
  I["info"] = '<rect class="f" x="4" y="4" width="16" height="16"/>' +
    '<rect class="s" x="11" y="7" width="2" height="2"/><rect x="11" y="11" width="2" height="6"/>';
  I["alert"] = '<path class="f" d="M10 4h4v3h2v4h2v4h2v5H4v-5h2v-4h2V7h2z"/>' +
    '<rect x="11" y="9" width="2" height="5"/><rect class="s" x="11" y="16" width="2" height="2"/>';
  I["alert-circle"] = '<rect class="f" x="4" y="4" width="16" height="16"/>' +
    '<rect x="11" y="7" width="2" height="6"/><rect class="s" x="11" y="15" width="2" height="2"/>';
  I["check-circle"] = '<rect class="f" x="4" y="4" width="16" height="16"/>' +
    '<path d="M16 8h2v2h-2v2h-2v2h-2v2h-2v-2H8v-2H6v-2h2v2h2v2h2v-2h2v-2h2z"/>';
  I["landmark"] = '<path class="f" d="M11 4h2v2h4v2h3v2H4V8h3V6h4z"/>' +
    '<path d="M6 10v7M10 10v7M14 10v7M18 10v7M4 17h16v3H4z"/>';
  I["banknote"] = '<rect class="f" x="3" y="6" width="18" height="12"/>' +
    '<rect x="9" y="9" width="6" height="6"/>' +
    '<rect class="s" x="5" y="8" width="2" height="2"/><rect class="s" x="17" y="14" width="2" height="2"/>';
  I["credit-card"] = '<rect class="f" x="3" y="5" width="18" height="14"/>' +
    '<path d="M3 9h18M6 14h5"/>';
  I["piggy"] = '<path class="f" d="M7 7h9v2h3V7h2v4h1v4h-2v3h-2v2h-2v-2h-5v2H9v-2H7v-2H5v-5h2z"/>' +
    '<rect class="s" x="16" y="10" width="2" height="2"/><path d="M3 12h2M10 9h4"/>';
  I["coins"] = '<rect class="f" x="4" y="5" width="10" height="5"/><path d="M4 10v4h10v-4M4 14v4h6"/>' +
    '<rect class="f" x="10" y="11" width="10" height="5"/><path d="M10 16v4h10v-4"/>';
  I["receipt"] = '<path class="f" d="M5 3h14v17l-2-2-2 2-3-2-3 2-2-2-2 2z"/>' +
    '<path d="M8 7h8M8 11h8M8 15h5"/>';
  I["calculator"] = '<rect class="f" x="5" y="3" width="14" height="18"/><rect x="8" y="6" width="8" height="4"/>' +
    '<rect class="s" x="8" y="12" width="2" height="2"/><rect class="s" x="11" y="12" width="2" height="2"/><rect class="s" x="14" y="12" width="2" height="2"/>' +
    '<rect class="s" x="8" y="16" width="2" height="2"/><rect class="s" x="11" y="16" width="2" height="2"/><rect class="s" x="14" y="16" width="2" height="2"/>';
  I["percent"] = '<path d="M17 5h2v2h-2v2h-2v2h-2v2h-2v2H9v2H7v2H5v-2h2v-2h2v-2h2v-2h2V9h2V7h2z"/>' +
    '<rect class="f" x="5" y="5" width="4" height="4"/><rect class="f" x="15" y="15" width="4" height="4"/>';
  I["download"] = '<path d="M11 4h2v9h-2zM7 10h2v2h2v2h2v-2h2v-2h2v2h-2v2h-2v2h-2v-2H9v-2H7z"/>' +
    '<path class="f" d="M4 16h16v4H4z"/>';
  I["upload"] = '<path d="M11 7h2v8h-2zM9 5h6v2H9zM7 7h2v2H7zM15 7h2v2h-2z"/>' +
    '<path class="f" d="M4 16h16v4H4z"/>';
  I["file"] = '<path class="f" d="M5 3h9l5 5v13H5z"/><path d="M14 3v5h5M8 12h8M8 16h5"/>';
  I["log-out"] = '<path d="M14 4h5v16h-5M4 11h10M11 8h2v2h2v4h-2v2h-2"/>';
  I["user"] = '<rect class="f" x="8" y="4" width="8" height="8"/><path class="f" d="M5 14h14v6H5z"/>';
  I["users"] = '<rect class="f" x="5" y="4" width="7" height="7"/><path class="f" d="M3 13h11v7H3z"/>' +
    '<rect x="14" y="5" width="5" height="6"/><path d="M16 14h5v6h-5"/>';
  I["mail"] = '<rect class="f" x="3" y="5" width="18" height="14"/><path d="M4 7h2v2h2v2h2v2h4v-2h2V9h2V7h2"/>';
  I["lock"] = '<rect class="f" x="5" y="10" width="14" height="10"/><path d="M8 10V6h8v4"/><rect class="s" x="11" y="14" width="2" height="3"/>';
  I["key"] = '<rect class="f" x="4" y="11" width="7" height="7"/><path d="M11 13h9v2h-3v3h-2v-3h-4z"/>';
  I["eye"] = '<path class="f" d="M6 6h12v2h2v2h2v4h-2v2h-2v2H6v-2H4v-2H2v-4h2V8h2z"/><rect x="9" y="9" width="6" height="6"/>';
  I["eye-off"] = '<path d="M6 6h12v2h2v2h2v4h-2v2h-2v2H6v-2H4v-2H2v-4h2V8h2zM4 4l16 16"/>';
  I["inbox"] = '<path class="f" d="M5 5h14l2 8v7H3v-7z"/><path d="M3 13h5l2 3h4l2-3h5"/>';
  I["history"] = '<path d="M7 4h10v2h2v2h1v8h-2v2h-2v2H8v-2H6v-2H4v-2h2v2h2v2h8v-2h2v-2h1V8h-2V6H7v2H5v2H3V6h2V4zM4 4h4v4H4zM11 8h2v5h3v2h-5z"/>';
  I["grid"] = '<rect class="f" x="4" y="4" width="7" height="7"/><rect x="13" y="4" width="7" height="7"/><rect x="4" y="13" width="7" height="7"/><rect class="f" x="13" y="13" width="7" height="7"/>';
  I["list"] = '<path d="M9 6h11M9 12h11M9 18h11"/><rect class="s" x="4" y="5" width="3" height="2"/><rect class="s" x="4" y="11" width="3" height="2"/><rect class="s" x="4" y="17" width="3" height="2"/>';
  I["scan"] = '<path d="M4 9V4h5M15 4h5v5M20 15v5h-5M9 20H4v-5"/><rect class="f" x="7" y="8" width="10" height="8"/><path d="M7 12h10"/>';
  I["camera"] = '<path class="f" d="M4 7h4l2-3h4l2 3h4v12H4z"/><rect x="9" y="10" width="6" height="6"/>';
  I["loader"] = '<path d="M9 4h6v2h2v2h2v6h-2v2h-2v2H9v-2H7v-2H5V8h2V6h2z"/>';
  I["more"] = '<rect class="s" x="4" y="10" width="4" height="4"/><rect class="s" x="10" y="10" width="4" height="4"/><rect class="s" x="16" y="10" width="4" height="4"/>';
  I["target"] = '<rect class="f" x="4" y="4" width="16" height="16"/><rect x="7" y="7" width="10" height="10"/><rect class="s" x="10" y="10" width="4" height="4"/>';
  I["book"] = '<rect class="f" x="5" y="4" width="14" height="14"/><path d="M5 18v3h14v-3M8 8h8"/>';
  I["book-open"] = '<path class="f" d="M3 5h8v14H3zM13 5h8v14h-8z"/><path d="M11 5h2v14h-2z"/>';
  I["contact"] = '<rect class="f" x="5" y="3" width="14" height="18"/><rect x="9" y="7" width="6" height="5"/><path d="M8 14h8v3H8zM3 7h2M3 12h2M3 17h2"/>';
  I["save"] = '<path class="f" d="M4 4h12l4 4v12H4z"/><path d="M7 4v5h7V4M7 20v-6h10v6"/>';
  I["shield"] = '<path class="f" d="M4 5h16v8h-2v3h-2v2h-2v2h-4v-2H8v-2H6v-3H4z"/>';
  I["shield-check"] = I["shield"] + '<path d="M9 12h2v2h2v-2h2v-2h2v-2h-2v2h-2v2h-2v-2H9z"/>';
  I["star"] = '<path class="f" d="M11 3h2v5h5v2h-3v3h1v5h-2v-2h-4v2H8v-5h1v-3H6V8h5z"/>';
  I["heart"] = '<path class="f" d="M5 5h4v2h2v2h2V7h2V5h4v2h2v5h-2v3h-2v2h-2v2h-2v2h-2v-2H9v-2H7v-2H5v-3H3V7h2z"/>';
  I["google"] = '<path d="M10 4h6v2h-6v2H8v2H6v4h2v2h2v2h6v-2h2v-2h-5v-2h8v4h-2v2h-2v2H8v-2H6v-2H4V8h2V6h2V4z"/>';
  I["power"] = '<rect x="11" y="3" width="2" height="9"/><path d="M7 6v2H5v8h2v2h10v-2h2V8h-2V6h-2v2h2v8H7V8h2V6z"/>';
  I["image"] = '<rect class="f" x="4" y="5" width="16" height="14"/><rect class="s" x="8" y="8" width="3" height="3"/><path d="M4 16l4-4 3 3 3-3 6 6"/>';
  I["flower"] = '<rect class="f" x="9" y="3" width="6" height="5"/><rect class="f" x="15" y="8" width="5" height="5"/><rect class="f" x="13" y="14" width="5" height="6"/><rect class="f" x="6" y="14" width="5" height="6"/><rect class="f" x="4" y="8" width="5" height="5"/><rect class="s" x="9" y="9" width="6" height="6"/>';
  I["globe"] = '<rect class="f" x="4" y="4" width="16" height="16"/><path d="M4 12h16M12 4v16M7 4v16M17 4v16"/>';

  // ---------- Ăn uống ----------
  I["coffee"] = '<path class="f" d="M5 9h11v9H5z"/><path d="M16 11h3v5h-3M4 20h14M8 4v3M12 4v3"/>';
  I["phin"] = '<rect class="f" x="7" y="4" width="10" height="7"/><path d="M5 4h14M5 11h14M9 11v3h6v-3"/><rect class="f" x="7" y="15" width="10" height="5"/>';
  I["milk-tea"] = '<path class="f" d="M6 8h12l-2 12H8z"/><path d="M5 8h14M8 8V5h8v3M12 3l2 5"/><rect class="s" x="9" y="16" width="2" height="2"/><rect class="s" x="12" y="17" width="2" height="2"/><rect class="s" x="13" y="14" width="2" height="2"/>';
  I["utensils"] = '<path d="M6 3v7h5V3M8.5 3v18"/><path class="f" d="M17 3h-3v7h3v10h1z"/>';
  I["pho"] = '<path class="f" d="M3 11h18v5h-2v3H5v-3H3z"/><path d="M7 20h10M15 3l-5 8M19 4l-7 7M7 8h4M13 8h3"/>';
  I["rice"] = '<path class="f" d="M4 12h16v4h-2v3H6v-3H4z"/><path class="f" d="M6 12V9h12v3"/><path d="M9 20h6"/>';
  I["banh-mi"] = '<path class="f" d="M4 12h4V9h8v3h4v5H4z"/><path d="M8 9h2v4H8zM13 9h2v4h-2z"/>';
  I["cup-soda"] = '<path class="f" d="M6 8h12l-2 12H8z"/><path d="M5 8h14M12 8l2-5h3M7 13h10"/>';
  I["beer"] = '<rect class="f" x="5" y="7" width="10" height="13"/><path d="M15 10h4v6h-4M6 7V4h8v3M9 11v6M12 11v6"/>';
  I["wine"] = '<path class="f" d="M7 4h10v6h-2v3h-6v-3H7z"/><path d="M12 13v7M8 20h8M7 8h10"/>';
  I["cake"] = '<rect class="f" x="4" y="12" width="16" height="8"/><path d="M4 16h16M11 8h2v4h-2zM7 9h2v3H7zM15 9h2v3h-2zM3 20h18"/><rect class="s" x="11" y="4" width="2" height="3"/>';
  I["ice-cream"] = '<path class="f" d="M7 7h10v5H7z"/><path d="M7 12h10l-5 9z"/><path d="M9 15h6"/>';
  I["pizza"] = '<path class="f" d="M4 6h16l-8 15z"/><path d="M5 9h14"/><rect class="s" x="9" y="11" width="2" height="2"/><rect class="s" x="13" y="13" width="2" height="2"/><rect class="s" x="11" y="16" width="2" height="2"/>';
  I["apple"] = '<path class="f" d="M6 7h5v2h2V7h5v10h-2v3H8v-3H6z"/><path d="M12 7V4h3"/>';
  I["cart"] = '<path d="M3 4h3l2 10h11l2-7H7"/><rect class="d" x="7" y="7" width="14" height="7"/><rect class="s" x="8" y="18" width="3" height="3"/><rect class="s" x="16" y="18" width="3" height="3"/>';
  I["basket"] = '<path class="f" d="M4 10h16l-2 10H6z"/><path d="M8 10l3-6M16 10l-3-6M9 14v4M12 14v4M15 14v4"/>';
  I["fish"] = '<path class="f" d="M4 12l4-5h7l4 3 2-3v10l-2-3-4 3H8z"/><rect class="s" x="8" y="11" width="2" height="2"/>';
  I["egg"] = '<path class="f" d="M9 4h6v3h2v4h1v5h-2v3H8v-3H6v-5h1V7h2z"/>';
  I["carrot"] = '<path class="f" d="M13 8h4v4l-9 9H5v-3z"/><path d="M14 8V4h3v4M17 8h4v3h-4"/>';
  I["candy"] = '<rect class="f" x="8" y="8" width="8" height="8"/><path d="M4 8h4v8H4zM16 8h4v8h-4z"/>';
  I["chef"] = '<path class="f" d="M6 5h12v9H6z"/><rect x="6" y="14" width="12" height="6"/><path d="M6 17h12"/>';

  // ---------- Di chuyển ----------
  I["scooter"] = '<rect class="f" x="4" y="15" width="4" height="4"/><rect class="f" x="16" y="15" width="4" height="4"/><path d="M6 17h6l2-5H9v5M14 12l2-6h3M16 6l2 11"/><rect class="d" x="8" y="12" width="5" height="5"/>';
  I["bike"] = '<rect class="f" x="3" y="14" width="5" height="5"/><rect class="f" x="16" y="14" width="5" height="5"/><path d="M5 16l4-7h6l3 7M9 9l3 7H5M15 9l1-3h2"/>';
  I["car"] = '<path class="f" d="M5 6h14v5h2v6H3v-6h2z"/><path d="M4 12h16M5 17v3M19 17v3"/><rect class="s" x="6" y="14" width="3" height="2"/><rect class="s" x="15" y="14" width="3" height="2"/>';
  I["taxi"] = I["car"] + '<rect x="10" y="3" width="4" height="3"/>';
  I["bus"] = '<rect class="f" x="5" y="4" width="14" height="14"/><path d="M5 11h14M7 18v2M17 18v2M8 7h8"/><rect class="s" x="7" y="13" width="2" height="2"/><rect class="s" x="15" y="13" width="2" height="2"/>';
  I["train"] = '<rect class="f" x="6" y="3" width="12" height="14"/><path d="M6 10h12M7 20l3-3M17 20l-3-3"/><rect class="s" x="8" y="13" width="2" height="2"/><rect class="s" x="14" y="13" width="2" height="2"/>';
  I["plane"] = '<path class="f" d="M11 3h2v6h7v3h-7v5h3v3H8v-3h3v-5H4V9h7z"/>';
  I["ship"] = '<path class="f" d="M3 14h18l-2 6H5z"/><path d="M6 14V9h12v5M9 9V5h6v4M12 3v2"/>';
  I["fuel"] = '<rect class="f" x="5" y="4" width="10" height="16"/><path d="M3 20h14M7 7h6v4H7zM15 11h3v6h2V9l-2-2"/>';
  I["parking"] = '<rect class="f" x="4" y="4" width="16" height="16"/><path d="M9 17V7h4a3 3 0 0 1 0 6H9"/>';
  I["wrench"] = '<path class="f" d="M14 4h4l-3 3 2 2 3-3v4h-3l-8 8H5v-4l8-8z"/>';
  I["map-pin"] = '<path class="f" d="M7 5h10v8h-2v3h-2v3h-2v-3H9v-3H7z"/><rect x="10" y="8" width="4" height="4"/>';
  I["compass"] = '<rect class="f" x="4" y="4" width="16" height="16"/><path d="M15 9l-2 5-4 2 2-5z"/>';
  I["luggage"] = '<rect class="f" x="6" y="7" width="12" height="12"/><path d="M9 7V4h6v3M10 10v6M14 10v6M8 19v2M16 19v2"/>';
  I["ticket"] = '<path class="f" d="M4 6h16v4h-2v4h2v4H4v-4h2v-4H4z"/><path d="M14 6v3M14 11v2M14 15v3"/>';
  I["road"] = '<path class="f" d="M7 4h10l3 16H4z"/><path d="M12 6v3M12 11v3M12 16v3"/>';

  // ---------- Nhà cửa ----------
  I["building"] = '<rect class="f" x="5" y="4" width="10" height="16"/><path d="M15 9h5v11h-5M3 20h18M8 7h3M8 11h3M8 15h3"/>';
  I["bolt"] = I["zap"];
  I["droplet"] = '<path class="f" d="M11 3h2v3h2v3h2v3h1v5h-2v3H8v-3H6v-5h1V9h2V6h2z"/><path d="M9 14v3h3"/>';
  I["wifi"] = '<path d="M3 8h18v2H3zM6 12h12v2H6zM9 16h6v2H9z"/><rect class="s" x="11" y="19" width="2" height="2"/>';
  I["tv"] = '<rect class="f" x="3" y="6" width="18" height="12"/><path d="M8 3l4 3 4-3M8 21h8"/>';
  I["bulb"] = '<path class="f" d="M8 4h8v2h2v6h-2v3H8v-3H6V6h2z"/><path d="M9 18h6M10 21h4"/>';
  I["bed"] = '<path class="f" d="M3 16V9h8v4h10v3z"/><path d="M3 5v15M21 13v7M3 16h18"/><rect x="5" y="10" width="3" height="3"/>';
  I["bath"] = '<path class="f" d="M3 12h18v5H5v-5z"/><path d="M6 12V5h4v2M7 17v3M17 17v3"/>';
  I["sofa"] = '<rect class="f" x="5" y="6" width="14" height="6"/><rect class="f" x="3" y="12" width="18" height="6"/><path d="M5 18v3M19 18v3"/>';
  I["fan"] = '<rect class="s" x="11" y="9" width="2" height="2"/><rect class="f" x="10" y="3" width="4" height="6"/><rect class="f" x="10" y="11" width="4" height="5"/><rect class="f" x="4" y="8" width="6" height="4"/><rect class="f" x="14" y="8" width="6" height="4"/><path d="M12 16v5M8 21h8"/>';
  I["plug"] = '<rect class="f" x="7" y="8" width="10" height="6"/><path d="M9 8V3M15 8V3M12 14v7"/>';
  I["flame"] = '<path class="f" d="M10 4h3v4h3v4h2v5h-2v3H8v-3H6v-5h2V8h2z"/><rect x="10" y="14" width="4" height="5"/>';
  I["tree"] = '<path class="f" d="M11 3h2v3h3v4h2v5H6v-5h2V6h3z"/><rect x="11" y="15" width="2" height="6"/>';
  I["sprout"] = '<rect class="f" x="4" y="6" width="7" height="6"/><rect class="f" x="13" y="4" width="7" height="6"/><path d="M12 20V10M8 20h8"/>';
  I["key-house"] = I["key"];
  I["trash-can"] = I["trash"];

  // ---------- Mua sắm ----------
  I["bag"] = '<rect class="f" x="5" y="8" width="14" height="12"/><path d="M8 8V4h8v4"/>';
  I["shirt"] = '<path class="f" d="M8 4h8l5 3-2 4-2-1v10H7V10l-2 1-2-4z"/>';
  I["ao-dai"] = '<path class="f" d="M9 4h6l3 4-2 2-1-1 2 11H7l2-11-1 1-2-2z"/><path d="M12 4v3M9 12h6"/>';
  I["shoe"] = '<path class="f" d="M3 16V8h5l3 4h7v4z"/><path d="M3 16v3h18v-3"/>';
  I["watch"] = '<rect class="f" x="7" y="7" width="10" height="10"/><path d="M9 7V3h6v4M9 17v4h6v-4M12 9v3h2"/>';
  I["glasses"] = '<rect class="f" x="3" y="11" width="7" height="6"/><rect class="f" x="14" y="11" width="7" height="6"/><path d="M10 13h4M3 13V8h3M21 13V8h-3"/>';
  I["gem"] = '<path class="f" d="M6 4h12l3 5-9 11L3 9z"/><path d="M3 9h18M9 4l-1 5 4 11 4-11-1-5"/>';
  I["gift"] = '<rect class="f" x="4" y="10" width="16" height="10"/><path d="M3 7h18v3H3zM12 7v13M8 4h3v3H8zM13 4h3v3h-3z"/>';
  I["scissors"] = '<rect class="f" x="4" y="4" width="5" height="5"/><rect class="f" x="4" y="15" width="5" height="5"/><path d="M9 8l11 9M9 16l11-9"/>';
  I["package"] = '<path class="f" d="M4 7l8-4 8 4v10l-8 4-8-4z"/><path d="M4 7l8 4 8-4M12 11v10"/>';
  I["lipstick"] = '<rect class="f" x="9" y="11" width="6" height="9"/><path d="M10 11V6l4-3v8M8 20h8"/>';
  I["store"] = '<rect class="f" x="4" y="10" width="16" height="10"/><path d="M3 4h18v6H3zM10 20v-5h4v5"/>';
  I["crown"] = '<path class="f" d="M4 8l4 3 4-6 4 6 4-3v10H4z"/><path d="M4 20h16"/>';
  I["umbrella"] = '<path class="f" d="M3 12h18V9h-2V6h-3V4H8v2H5v3H3z"/><path d="M12 12v7h-3"/>';
  I["non-la"] = '<path class="f" d="M11 4h2l8 12H3z"/><path d="M3 16h18"/>';

  // ---------- Công nghệ ----------
  I["phone"] = '<rect class="f" x="6" y="3" width="12" height="18"/><path d="M10 6h4"/><rect class="s" x="11" y="17" width="2" height="2"/>';
  I["laptop"] = '<rect class="f" x="4" y="5" width="16" height="10"/><path d="M2 18h20l-2-3H4z"/>';
  I["headphones"] = '<path d="M4 14V8h16v6"/><rect class="f" x="3" y="13" width="5" height="7"/><rect class="f" x="16" y="13" width="5" height="7"/>';
  I["gamepad"] = '<path class="f" d="M5 7h14v2h2v7h-2v2h-3l-2-2h-4l-2 2H5v-2H3V9h2z"/>' +
    '<path d="M7 10v4M5 12h4"/>' +
    '<rect class="s" x="14" y="10" width="2" height="2"/><rect class="s" x="17" y="12" width="2" height="2"/>';
  I["monitor"] = '<rect class="f" x="3" y="4" width="18" height="12"/><path d="M12 16v4M8 20h8"/>';
  I["printer"] = '<path class="f" d="M4 9h16v8H4z"/><path d="M7 9V4h10v5M7 14h10v6H7z"/>';
  I["radio"] = '<rect class="f" x="3" y="8" width="18" height="12"/><path d="M6 8l11-4"/><rect x="14" y="12" width="5" height="5"/><path d="M6 12h5M6 15h5"/>';
  I["cassette"] = '<rect class="f" x="3" y="6" width="18" height="12"/><rect x="6" y="9" width="4" height="4"/><rect x="14" y="9" width="4" height="4"/><path d="M10 11h4M6 18l2-3h8l2 3"/>';

  // ---------- Tài chính ----------
  I["briefcase"] = '<rect class="f" x="3" y="7" width="18" height="13"/><path d="M8 7V4h8v3M3 13h18M10 13v2h4v-2"/>';
  I["hand-coins"] = '<rect class="f" x="12" y="4" width="7" height="7"/><path d="M3 14h10v3h6v3H5l-2-2z"/>';
  I["safe"] = '<rect class="f" x="4" y="4" width="16" height="14"/><rect x="9" y="8" width="6" height="6"/><path d="M7 18v2M17 18v2"/>';
  I["handshake"] = '<path class="f" d="M3 11l5-5 4 2 4-2 5 5-7 7h-4z"/><path d="M9 11l3 3 3-3"/>';

  // ---------- Sức khỏe ----------
  I["heart-pulse"] = I["heart"] + '<path d="M5 12h3l2-3 3 6 2-3h4"/>';
  I["pill"] = '<path class="f" d="M5 12l7-7h5v5l-7 7H5z"/><path d="M9 8l7 7"/>';
  I["stethoscope"] = '<path d="M5 4v6h8V4M9 10v5h9v-3"/><rect class="f" x="16" y="9" width="5" height="4"/>';
  I["dumbbell"] = '<rect class="f" x="3" y="9" width="3" height="6"/><rect class="f" x="18" y="9" width="3" height="6"/><rect x="6" y="7" width="3" height="10"/><rect x="15" y="7" width="3" height="10"/><path d="M9 12h6"/>';
  I["activity"] = '<path d="M3 12h4l2-6 4 12 3-6h5"/>';
  I["syringe"] = '<path class="f" d="M13 5l6 6-8 8-6-6z"/><path d="M17 3l4 4M3 21l4-4M10 10l3 3"/>';
  I["tooth"] = '<path class="f" d="M5 5h14v8h-2v7h-3l-2-4-2 4H7v-7H5z"/>';
  I["baby"] = '<rect class="f" x="5" y="5" width="14" height="14"/><path d="M9 15h6M11 5V3h2"/><rect class="s" x="8" y="10" width="2" height="2"/><rect class="s" x="14" y="10" width="2" height="2"/>';
  I["cross"] = '<path class="f" d="M9 4h6v5h5v6h-5v5H9v-5H4V9h5z"/>';
  I["yoga"] = '<rect class="f" x="10" y="4" width="4" height="4"/><path d="M12 8v6M5 10l7 2 7-2M7 20l5-6 5 6M4 20h16"/>';

  // ---------- Giải trí ----------
  I["film"] = '<rect class="f" x="3" y="4" width="18" height="16"/><path d="M7 4v16M17 4v16M3 8h4M3 12h18M3 16h4M17 8h4M17 16h4"/>';
  I["music"] = '<path d="M9 17V5l11-2v12"/><rect class="f" x="4" y="15" width="5" height="5"/><rect class="f" x="15" y="13" width="5" height="5"/><path d="M9 9l11-2"/>';
  I["guitar"] = '<path class="f" d="M4 13h7v7H4z"/><path d="M11 13l8-8M18 3l3 3"/>';
  I["party"] = '<path class="f" d="M4 20l4-12 8 8z"/><path d="M14 4h2v2h-2zM19 8h2v2h-2zM17 12h2v2h-2z"/>';
  I["paw"] = '<rect class="f" x="7" y="12" width="10" height="8"/><rect class="f" x="4" y="8" width="3" height="4"/><rect class="f" x="8" y="4" width="3" height="4"/><rect class="f" x="13" y="4" width="3" height="4"/><rect class="f" x="17" y="8" width="3" height="4"/>';
  I["cat"] = '<path class="f" d="M5 4l3 3h8l3-3v14H5z"/><rect class="s" x="8" y="11" width="2" height="2"/><rect class="s" x="14" y="11" width="2" height="2"/><path d="M11 15h2M2 14h3M19 14h3"/>';
  I["dog"] = '<rect class="f" x="6" y="5" width="12" height="14"/><path d="M3 6h3v7H3zM18 6h3v7h-3zM10 15h4"/><rect class="s" x="9" y="10" width="2" height="2"/><rect class="s" x="13" y="10" width="2" height="2"/>';
  I["palm"] = '<path d="M12 20V9M8 20h8"/><rect class="f" x="4" y="6" width="8" height="3"/><rect class="f" x="12" y="6" width="8" height="3"/><rect class="f" x="7" y="4" width="10" height="3"/>';
  I["tent"] = '<path class="f" d="M12 4l9 16H3z"/><path d="M12 11l-3 9h6z"/>';
  I["mountain"] = '<path class="f" d="M3 20l6-11 4 6 3-4 5 9z"/>';
  I["dice"] = '<rect class="f" x="4" y="4" width="16" height="16"/><rect class="s" x="7" y="7" width="2" height="2"/><rect class="s" x="15" y="7" width="2" height="2"/><rect class="s" x="11" y="11" width="2" height="2"/><rect class="s" x="7" y="15" width="2" height="2"/><rect class="s" x="15" y="15" width="2" height="2"/>';
  I["vinyl"] = '<rect class="f" x="4" y="4" width="16" height="16"/><rect x="9" y="9" width="6" height="6"/>';
  I["disco"] = '<rect class="f" x="5" y="6" width="14" height="14"/><path d="M12 3v3M5 13h14M12 6v14M8 9h8M8 17h8"/>';

  // ---------- Học tập ----------
  I["graduation"] = '<path class="f" d="M3 9l9-4 9 4-9 4z"/><path d="M7 11v6h10v-6M21 9v6"/>';
  I["pen"] = I["pencil"];
  I["school"] = '<rect class="f" x="5" y="9" width="14" height="11"/><path d="M5 9l7-4 7 4M3 20h18M12 5V2h3M10 20v-5h4v5"/>';
  I["lamp"] = '<path class="f" d="M7 4h10l3 7H4z"/><path d="M12 11v8M8 20h8"/>';

  // ---------- Gia đình & Xã hội ----------
  I["phone-call"] = '<path class="f" d="M5 4h4l2 5-2 2 4 4 2-2 5 2v5H8L4 12z"/><path d="M14 4h6v6M14 7h3v3"/>';
  I["envelope-red"] = '<rect class="f" x="5" y="3" width="14" height="18"/><path d="M5 8h14"/><rect x="10" y="11" width="4" height="4"/>';
  I["pagoda"] = '<path class="f" d="M5 9h14l-2-3H7zM6 14h12l-2-3H8z"/><path d="M12 3v3M8 14v6h8v-6M4 20h16"/>';
  I["flag"] = '<path class="f" d="M6 4h13v8H6z"/><path d="M6 20V4"/>';
  I["home-heart"] = I["home"] + '<rect class="s" x="10" y="10" width="4" height="4"/>';

  // ---------- Thu nhập ----------
  I["salary"] = '<rect class="f" x="3" y="6" width="18" height="12"/><path d="M11 9h2v6h-2zM9 10h6M9 14h6"/>';
  I["chart-up"] = I["trend-up"];

  // ===== Bí danh: giữ nguyên 100% tương thích tên cũ =====
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
    "utensils-crossed": "utensils", "soup": "pho", "cooking-pot": "pho", "sandwich": "banh-mi", "croissant": "banh-mi", "cookie": "candy", "candy": "candy", "nut": "candy", "citrus": "apple", "banana": "apple", "cherry": "apple", "grape": "apple",
    "martini": "wine", "glass-water": "cup-soda", "milk": "cup-soda", "sparkling-wine": "wine", "beef": "chef", "drumstick": "chef", "wheat": "rice", "popcorn": "candy", "refrigerator": "store", "microwave": "store", "oven": "store",
    "chef-hat": "chef", "concierge-bell": "chef", "shopping-cart": "cart", "shopping-basket": "basket", "scale": "calculator",
    "car-front": "car", "car-taxi-front": "taxi", "cable-car": "train", "tram-front": "train", "rail-symbol": "train", "caravan": "bus", "truck": "bus", "sailboat": "ship", "anchor": "ship", "ferry": "ship",
    "helicopter": "plane", "rocket": "plane", "plane-takeoff": "plane", "plane-landing": "plane", "map": "map-pin", "navigation": "compass", "navigation-2": "compass", "milestone": "road", "route": "road", "signpost": "road",
    "traffic-cone": "road", "gauge": "scooter", "circle-parking": "parking", "tickets": "ticket", "footprints": "shoe", "battery-charging": "plug", "hammer": "wrench", "plug-zap": "plug", "flashlight": "bulb", "siren": "alert", "circle-dot": "target",
    "building-2": "building", "warehouse": "building", "factory": "building", "droplets": "droplet", "lightbulb": "bulb", "door-closed": "home", "door-open": "home", "fence": "home",
    "air-vent": "fan", "thermometer": "droplet", "wind": "fan", "shower-head": "bath", "waves": "droplet", "paint-bucket": "palette", "brush": "palette", "roller-coaster": "party", "shovel": "sprout",
    "axe": "wrench", "drill": "wrench", "tree-pine": "tree", "trees": "tree", "flower-2": "flower", "plug-2": "plug",
    "shopping-bag": "bag", "package-check": "package", "package-open": "package", "package-plus": "package", "boxes": "package", "box": "package", "archive": "package", "hanger": "shirt", "ribbon": "gift",
    "badge": "star", "award": "star", "trophy": "crown", "medal": "star", "smile": "heart", "sun-medium": "sun", "layers": "package",
    "smartphone": "phone", "smartphone-charging": "phone", "laptop-2": "laptop", "tablet": "phone", "monitor-speaker": "monitor", "monitor-smartphone": "monitor", "cpu": "laptop", "hard-drive": "laptop",
    "battery": "plug", "battery-full": "plug", "battery-low": "plug", "battery-warning": "plug", "server": "monitor", "database": "monitor", "router": "wifi", "bluetooth": "wifi", "mic": "radio", "mic-2": "radio",
    "video": "camera", "webcam": "camera", "cast": "tv", "mouse": "laptop", "keyboard": "laptop", "usb": "plug", "gamepad-2": "gamepad", "code": "laptop", "terminal": "laptop", "binary": "laptop",
    "cloud-download": "download", "cloud-upload": "cloud-up", "satellite": "radio", "signal": "wifi", "fingerprint": "lock", "bot": "flower",
    "bandage": "cross", "dna": "heart-pulse", "brain": "bulb", "ear": "heart", "accessibility": "yoga", "heart-handshake": "handshake", "bike": "bike", "swords": "dumbbell", "smile-plus": "heart",
    "music-2": "music", "music-3": "music", "music-4": "music", "clapperboard": "film", "party-popper": "party", "palmtree": "palm", "paw-print": "paw", "dices": "dice", "disc": "vinyl",
    "ferris-wheel": "party", "binoculars": "compass", "drum": "music", "piano": "music",
    "graduation-cap": "graduation", "folder": "file", "folder-open": "file", "presentation": "monitor", "projector": "monitor", "university": "school", "ruler": "pencil", "stamp": "star",
    "paperclip": "file", "bookmark": "book", "microscope": "bulb", "test-tube": "bulb", "flask-conical": "bulb", "atom": "bulb", "languages": "globe",
    "phone-call": "phone-call", "send": "mail", "share-2": "swap", "church": "pagoda", "help-circle": "info", "laugh": "heart", "inbox": "inbox", "mail": "mail",
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

  // ---------- Bảng màu Sprout Lands Cozy Pastel tương phản cao ----------
  var P = {
    mu: "#F8D56B", ye: "#FFE88A", or: "#F4A46A", tm: "#EE7A74", ro: "#F4A0B5", pk: "#FAD0DA",
    av: "#9CE07E", gr: "#6CC570", te: "#67C5AE", mt: "#A9EEDB", sk: "#8ED6F5", dn: "#6AA4EB",
    cr: "#FBF2DE", wh: "#FFFDF7", br: "#C8945C", cf: "#6B4226", pl: "#C2A6F2", gy: "#D8CEBE",
    ink: "currentColor"
  };
  function shade(hex) {
    var n = parseInt(hex.slice(1), 16), f = 0.78;
    var r = Math.round(((n >> 16) & 255) * f), g = Math.round(((n >> 8) & 255) * f), b = Math.round((n & 255) * f);
    return "#" + ((1 << 24) | (r << 16) | (g << 8) | b).toString(16).slice(1);
  }
  function col(t) { return t === "ink" ? "currentColor" : "var(--ic-" + t + "," + (P[t] || P.mu) + ")"; }

  var C = {
    "home": "or", "chart": "te mu ro", "pie": "mu", "trend-up": "av", "tags": "mu wh", "wallet": "or", "settings": "mu",
    "palette": "cr tm te dn", "sun": "ye", "moon": "ye", "cloud": "sk", "cloud-up": "sk", "zap": "ye", "bell": "mu",
    "chat": "sk", "sparkles": "ye", "trash": "ro", "pencil": "ye", "search": "sk", "filter": "te", "calendar": "cr tm te ink",
    "clock": "cr", "info": "sk ink", "alert": "ye ink", "alert-circle": "ro ink", "check-circle": "av", "landmark": "tm",
    "banknote": "av", "credit-card": "dn", "piggy": "pk ink", "coins": "ye mu", "receipt": "cr", "calculator": "te mu or tm av sk pl",
    "percent": "ro te", "download": "sk", "upload": "sk", "file": "cr", "user": "ye te", "users": "or te", "mail": "cr",
    "lock": "mu ink", "key": "ye", "eye": "wh", "inbox": "sk", "grid": "or te", "list": "or te mu", "scan": "sk", "camera": "dn",
    "more": "or mu te", "target": "wh tm", "book": "te", "book-open": "cr sk", "contact": "cr", "save": "dn", "shield": "te",
    "star": "ye", "heart": "tm", "image": "sk ye", "flower": "pk ro pk ro pk ye", "globe": "sk",
    "coffee": "cr", "phin": "gy br", "milk-tea": "pk cf cf cf", "utensils": "gy", "pho": "cr", "rice": "dn wh", "banh-mi": "mu",
    "cup-soda": "tm", "beer": "ye", "wine": "pl", "cake": "pk tm", "ice-cream": "pk", "pizza": "ye tm tm tm", "apple": "tm",
    "cart": "mu ink ink", "basket": "mu", "fish": "sk cf", "egg": "wh", "carrot": "or", "candy": "ro", "chef": "wh",
    "scooter": "sk sk tm", "bike": "cf cf", "car": "tm ye ye", "taxi": "ye wh wh", "bus": "or ye ye", "train": "te ye ye",
    "plane": "sk", "ship": "tm", "fuel": "tm", "parking": "dn", "wrench": "gy", "map-pin": "tm", "compass": "cr", "luggage": "or",
    "ticket": "or", "road": "gy",
    "building": "mu", "droplet": "sk", "wifi": "te", "tv": "cf", "bulb": "ye", "bed": "dn", "bath": "wh", "sofa": "te or",
    "fan": "sk mt sk mt sk", "plug": "gy", "flame": "or", "tree": "gr", "sprout": "av gr", "bag": "or", "shirt": "te", "ao-dai": "ro",
    "shoe": "tm", "watch": "cr", "glasses": "sk sk", "gem": "mt", "gift": "ro", "scissors": "tm tm", "package": "mu", "lipstick": "ro",
    "store": "cr", "crown": "ye", "umbrella": "tm", "non-la": "ye",
    "phone": "sk ink", "laptop": "sk", "headphones": "or or", "gamepad": "pl ye ro", "monitor": "sk", "printer": "gy", "radio": "or", "cassette": "mu",
    "briefcase": "br", "hand-coins": "ye", "safe": "gy", "handshake": "pk", "salary": "av",
    "heart-pulse": "tm", "pill": "wh", "stethoscope": "sk", "dumbbell": "tm tm", "syringe": "sk", "tooth": "wh", "baby": "pk cf cf",
    "cross": "tm", "yoga": "or",
    "film": "pl", "music": "or te", "guitar": "or", "party": "ye", "paw": "br ro ro ro ro", "cat": "mu cf cf", "dog": "br cf cf",
    "palm": "gr av gr", "tent": "or", "mountain": "te", "dice": "wh tm tm tm tm tm", "vinyl": "pl", "disco": "sk",
    "graduation": "dn", "school": "tm", "lamp": "mu", "phone-call": "av", "envelope-red": "tm", "pagoda": "tm", "flag": "tm",
    "home-heart": "or tm"
  };

  var X = {
    "home": [["tm", '<path d="M9 4h6v2h2v2H7V6h2z"/>'], ["mu", '<rect x="10" y="15" width="4" height="5"/>']],
    "pie": [["te", '<path d="M10 5h1v8h8v2h-2v2h-2v2h-6v-2H7v-2H5V9h2V7h3z"/>']],
    "wallet": [["mu", '<rect x="15" y="11" width="6" height="5"/>']],
    "settings": [["cr", '<rect x="9" y="9" width="6" height="6"/>']],
    "sun": [["or", '<rect x="10" y="10" width="4" height="4"/>']],
    "calendar": [["tm", '<rect x="4" y="5" width="16" height="5"/>']],
    "banknote": [["ye", '<rect x="9" y="9" width="6" height="6"/>']],
    "credit-card": [["ye", '<rect x="3" y="5" width="18" height="4"/>'], ["mu", '<rect x="5" y="13" width="4" height="3"/>']],
    "camera": [["sk", '<rect x="9" y="10" width="6" height="6"/>']],
    "heart": [["wh", '<rect x="6" y="7" width="2" height="2"/>']],
    "sparkles": [["ro", '<path d="M17 13h2v2h2v2h-2v2h-2v-2h-2v-2h2z"/>']],
    "coffee": [["br", '<rect x="5" y="9" width="11" height="3"/>']],
    "pho": [["or", '<rect x="4" y="11" width="16" height="3"/>'], ["gr", '<rect x="8" y="9" width="2" height="2"/><rect x="14" y="9" width="2" height="2"/>']],
    "bag": [["mu", '<rect x="5" y="9" width="14" height="3"/>']],
    "gift": [["ye", '<rect x="3" y="7" width="18" height="3"/>'], ["mu", '<rect x="11" y="7" width="2" height="13"/>']],
    "gamepad": [["te", '<rect x="6" y="10" width="2" height="4"/><rect x="5" y="11" width="4" height="2"/>']],
    "star": [["mu", '<rect x="11" y="5" width="2" height="11"/>']],
    "salary": [["ye", '<rect x="9" y="9" width="6" height="6"/>']],
    "cross": [["wh", '<rect x="11" y="6" width="2" height="12"/><rect x="6" y="11" width="12" height="2"/>']]
  };
  X["bell-ring"] = X["bell"]; C["bell-ring"] = C["bell"];
  X["key-house"] = X["key"]; C["key-house"] = C["key"];
  X["trash-can"] = X["trash"]; C["trash-can"] = C["trash"];
  C["pen"] = C["pencil"]; X["pen"] = X["pencil"];
  C["tag"] = C["tags"]; C["bolt"] = C["zap"]; C["chart-up"] = C["trend-up"];
  C["shield-check"] = C["shield"]; X["shield-check"] = X["shield"];
  C["heart-pulse"] = C["heart"];

  var EL = /<(path|rect|circle|ellipse)\b([^>]*?)\/>/g;
  function paint(el, attrs, fill, stroke) {
    return "<" + el + attrs.replace(/\sclass="\w"/, "") + ' style="fill:' + fill + ";stroke:" + stroke + '"/>';
  }
  var cache = {};
  function build(key, mono) {
    var body = I[key], colors = (C[key] || "").split(" ").filter(Boolean), k = 0;
    var under = "", lines = "";
    EL.lastIndex = 0;
    var top = body.replace(EL, function (all, el, attrs) {
      var c = (attrs.match(/class="(\w)"/) || [])[1];
      if (!c) return all;
      var tok = colors[k++] || (c === "s" ? "ink" : "mu");
      if (mono) {
        if (c === "s") return paint(el, attrs, "currentColor", "none");
        under += paint(el, attrs, "currentColor;fill-opacity:.22", "none");
        return c === "d" ? "" : paint(el, attrs, "none", "currentColor");
      }
      if (c === "s") return paint(el, attrs, col(tok), "none");
      if (P[tok] && tok !== "ink") {
        // Bóng khối pixel 1px góc dưới-phải + nền pastel sáng
        under += paint(el, attrs.replace(/\/?$/, "") + ' transform="translate(1 1)"', shade(P[tok]), "none");
        under += paint(el, attrs, col(tok), "none");
      } else {
        under += paint(el, attrs, col(tok), "none");
      }
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
    return '<svg class="vi ' + (cls || "") + '" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="square" stroke-linejoin="miter" shape-rendering="crispEdges" aria-hidden="true">' + cache[ck] + "</svg>";
  }

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
