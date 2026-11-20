# Nhóm HỆ THỐNG – vector 32×32

icon("settings", """
<path d="M13.5 3h5l.8 3.6 2.4 1 3.1-2 3.5 3.5-2 3.1 1 2.4 3.7.9v5l-3.7.8-1 2.4 2 3.1-3.5 3.5-3.1-2-2.4 1-.8 3.7h-5l-.8-3.7-2.4-1-3.1 2L3.6 25l2-3.1-1-2.4L1 18.7v-5l3.6-.9 1-2.4-2-3.1L7.1 3.8l3.1 2 2.4-1z" fill="s"/>
<circle cx="16" cy="16.2" r="6" fill="S"/><circle cx="16" cy="16.2" r="3" fill="W"/>
""")

icon("sun", """
<path d="M16 2v4M16 26v4M2 16h4M26 16h4M6 6l2.8 2.8M23.2 23.2L26 26M6 26l2.8-2.8M23.2 8.8L26 6" stroke="O" stroke-width="2.4"/>
<circle cx="16" cy="16" r="8" fill="y"/>
<circle cx="13" cy="15" r=".9" fill="D"/><circle cx="19" cy="15" r=".9" fill="D"/>
<path d="M13.5 18.5c1.5 1.4 3.5 1.4 5 0" stroke="D" stroke-width="1" fill="none"/>
""")

icon("moon", """
<path d="M21 4a12 12 0 1 0 7 17A10 10 0 0 1 21 4z" fill="y"/>
<circle cx="11" cy="13" r="1.6" fill="Y"/><circle cx="15" cy="22" r="1.2" fill="Y"/>
<path d="M25 9v3M23.5 10.5h3M27 15.5v2M26 16.5h2" stroke="b" stroke-width="1"/>
""")

CLOUD = '<path d="M9 26a6 6 0 01-.8-12A8 8 0 0123.5 11.5 6.5 6.5 0 0123.5 26z" fill="{c}"/>'
icon("cloud", CLOUD.format(c="W"))
icon("cloud-up", CLOUD.format(c="W") + '<path d="M16 23v-9M12 17.5l4-4 4 4" stroke="B" stroke-width="2.4" fill="none"/>')
icon("cloud-off", CLOUD.format(c="s") + '<path d="M4 4l24 24" stroke="r" stroke-width="2.6"/>')

icon("zap", """
<path d="M19 2L6 18h8.5L12 30l14-17h-8.5z" fill="y"/>
""")

BELL = """<circle cx="16" cy="4" r="1.8" fill="{c}"/>
<path d="M16 5c-5 0-8 3.5-8 8.5v6L5 23.5v1.5h22v-1.5l-3-4v-6C24 8.5 21 5 16 5z" fill="{c}"/>
<path d="M12.5 26.5a3.5 3.5 0 007 0z" fill="O"/>"""
icon("bell", BELL.format(c="y"))
icon("bell-ring", BELL.format(c="y") + '<path d="M4 9a9 9 0 013-5.5M28 9a9 9 0 00-3-5.5M2.5 14a13 13 0 011-4M29.5 14a13 13 0 00-1-4" stroke="P" stroke-width="1.6" fill="none"/>')
icon("bell-off", BELL.format(c="s") + '<path d="M4 4l24 24" stroke="r" stroke-width="2.6"/>')

icon("chat", """
<path d="M7 4h18a4 4 0 014 4v10a4 4 0 01-4 4H14l-7 6v-6a4 4 0 01-4-4V8a4 4 0 014-4z" fill="l"/>
<circle cx="10.5" cy="13" r="1.8" fill="W"/><circle cx="16" cy="13" r="1.8" fill="W"/><circle cx="21.5" cy="13" r="1.8" fill="W"/>
""")

icon("sparkles", """
<path d="M12 5c.8 5 2.5 7.2 7.5 8-5 .8-6.7 3-7.5 8-.8-5-2.5-7.2-7.5-8 5-.8 6.7-3 7.5-8z" fill="y"/>
<path d="M24 3c.4 2.6 1.3 3.6 4 4-2.7.4-3.6 1.4-4 4-.4-2.6-1.3-3.6-4-4 2.7-.4 3.6-1.4 4-4z" fill="p"/>
<path d="M23 19c.4 2.6 1.3 3.6 4 4-2.7.4-3.6 1.4-4 4-.4-2.6-1.3-3.6-4-4 2.7-.4 3.6-1.4 4-4z" fill="b"/>
""")

icon("trash", """
<path d="M12.5 5V3.5h7V5" stroke="S" stroke-width="2" fill="none"/>
<rect x="4" y="5" width="24" height="4" rx="1.5" fill="S"/>
<path d="M6 9h20l-1.8 19.5H7.8z" fill="s"/>
<path d="M11 12.5v12.5M16 12.5v12.5M21 12.5v12.5" stroke="S" stroke-width="1.4"/>
""")

icon("calendar", """
<rect x="3" y="5" width="26" height="24" rx="2.5" fill="W"/>
<path d="M3 7.5A2.5 2.5 0 015.5 5h21A2.5 2.5 0 0129 7.5V11H3z" fill="r"/>
<rect x="8" y="2.5" width="2.5" height="6" rx="1.2" fill="S"/><rect x="21.5" y="2.5" width="2.5" height="6" rx="1.2" fill="S"/>
<rect x="6" y="14" width="3.5" height="3" rx=".5" fill="s"/><rect x="11" y="14" width="3.5" height="3" rx=".5" fill="s"/><rect x="16" y="14" width="3.5" height="3" rx=".5" fill="s"/><rect x="21" y="14" width="3.5" height="3" rx=".5" fill="s"/>
<rect x="6" y="19" width="3.5" height="3" rx=".5" fill="s"/><rect x="11" y="19" width="3.5" height="3" rx=".5" fill="s"/><rect x="16" y="19" width="3.5" height="3" rx=".5" fill="r"/><rect x="21" y="19" width="3.5" height="3" rx=".5" fill="s"/>
<rect x="6" y="24" width="3.5" height="3" rx=".5" fill="s"/><rect x="11" y="24" width="3.5" height="3" rx=".5" fill="s"/>
""")

icon("clock", """
<path d="M4 9a5 5 0 017-5M28 9a5 5 0 00-7-5" stroke="r" stroke-width="3" fill="none"/>
<path d="M9 25l-3 4M23 25l3 4" stroke="n" stroke-width="2"/>
<circle cx="16" cy="17" r="11" fill="r"/>
<circle cx="16" cy="17" r="8.5" fill="W"/>
<path d="M16 17v-5.5M16 17l3.5 2" stroke="n" stroke-width="1.6" fill="none"/>
""")

BADGE = '<circle cx="16" cy="16" r="13" fill="{c}"/>'
icon("info", BADGE.format(c="B") + '<circle cx="16" cy="9.5" r="2" fill="W"/><rect x="14" y="13" width="4" height="11" rx="1" fill="W"/>')
icon("alert-circle", BADGE.format(c="r") + '<rect x="14" y="7.5" width="4" height="11" rx="1" fill="W"/><circle cx="16" cy="23" r="2" fill="W"/>')
icon("check-circle", BADGE.format(c="g") + '<path d="M9.5 16.5l4.5 4.5 8.5-9" stroke="W" stroke-width="3" fill="none"/>')
icon("alert", """
<path d="M16 3.5c1 0 1.8.5 2.3 1.4l10.3 18.4c1 1.8-.3 4-2.3 4H5.7c-2 0-3.3-2.2-2.3-4L13.7 4.9c.5-.9 1.3-1.4 2.3-1.4z" fill="y"/>
<rect x="14.2" y="10" width="3.6" height="9" rx="1" fill="n"/><circle cx="16" cy="23" r="1.9" fill="n"/>
""")

icon("inbox", """
<rect x="8" y="3" width="16" height="13" rx="1" fill="W"/>
<path d="M11 7h10M11 10.5h7" stroke="s" stroke-width="1.2"/>
<path d="M3 16l4-7h3v7zM29 16l-4-7h-3v7z" fill="l"/>
<path d="M3 15h7l2 3.5h8l2-3.5h7v12a2 2 0 01-2 2H5a2 2 0 01-2-2z" fill="l"/>
""")

icon("save", """
<path d="M3 5a2 2 0 012-2h18l6 6v18a2 2 0 01-2 2H5a2 2 0 01-2-2z" fill="B"/>
<rect x="8" y="3" width="14" height="8.5" fill="s"/><rect x="17.5" y="4.5" width="3" height="5.5" fill="S"/>
<rect x="7" y="16" width="18" height="13" rx="1" fill="W"/>
<path d="M10 20h12M10 24h12" stroke="s" stroke-width="1.2"/>
""")

icon("upload", """
<path d="M3 18h6v5h14v-5h6v9a2 2 0 01-2 2H5a2 2 0 01-2-2z" fill="l"/>
<path d="M16 20V7M10 12.5L16 6l6 6.5" stroke="g" stroke-width="3.4" fill="none"/>
""")

icon("history", """
<rect x="6" y="2.5" width="20" height="3.5" rx="1" fill="C"/><rect x="6" y="26" width="20" height="3.5" rx="1" fill="C"/>
<path d="M8 6h16c0 5-3 8-6 10 3 2 6 5 6 10H8c0-5 3-8 6-10-3-2-6-5-6-10z" fill="b"/>
<path d="M11 9h10c-1 2.5-2.8 4-5 5.5C13.8 13 12 11.5 11 9z" fill="c"/>
<path d="M10.5 25c.8-2.5 3-4.5 5.5-5 2.5.5 4.7 2.5 5.5 5z" fill="c"/>
<path d="M16 15v5" stroke="c" stroke-width="1"/>
""")

icon("mic", """
<path d="M8 14a8 8 0 0016 0" stroke="S" stroke-width="2.2" fill="none"/>
<path d="M16 22v5M11 28h10" stroke="S" stroke-width="2.2"/>
<rect x="11" y="2.5" width="10" height="17" rx="5" fill="p"/>
<path d="M13 8h6M13 11h6M13 14h6" stroke="P" stroke-width="1"/>
""")

icon("wrench", """
<path d="M6.5 25.5l11-11" stroke="s" stroke-width="5"/>
<path d="M21 3.5a7.5 7.5 0 00-4.5 11l4 4a7.5 7.5 0 008-12.5l-4 4-3.5-.5-.5-3.5 4-4c-1.1-.3-2.3-.5-3.5 1.5z" fill="s"/>
<circle cx="6.5" cy="25.5" r="1.2" fill="S"/>
""")

icon("mai", """
<path d="M16 8.5L10 3M16 8.5l6-5.5" stroke="n" stroke-width="1.4"/>
<circle cx="10" cy="3" r="1.5" fill="y"/><circle cx="22" cy="3" r="1.5" fill="y"/>
<rect x="2.5" y="8" width="27" height="19" rx="3.5" fill="p"/>
<rect x="5.5" y="11" width="16.5" height="13" rx="2.5" fill="t"/>
<rect x="8.5" y="15" width="2" height="3" rx=".6" fill="W"/><rect x="16.5" y="15" width="2" height="3" rx=".6" fill="W"/>
<path d="M11.5 20c1.2 1 3.3 1 4.5 0" stroke="W" stroke-width="1.2" fill="none"/>
<circle cx="25.5" cy="14" r="1.6" fill="y"/><circle cx="25.5" cy="19.5" r="1.6" fill="M"/>
<rect x="7" y="27" width="3" height="2.5" fill="n"/><rect x="22" y="27" width="3" height="2.5" fill="n"/>
""")
