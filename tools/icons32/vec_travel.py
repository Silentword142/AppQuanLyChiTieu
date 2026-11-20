# Nhóm DI CHUYỂN – vector 32×32

WHEELS = '<circle cx="9" cy="24" r="3.6" fill="n"/><circle cx="9" cy="24" r="1.5" fill="s"/><circle cx="23" cy="24" r="3.6" fill="n"/><circle cx="23" cy="24" r="1.5" fill="s"/>'

icon("scooter", """
<path d="M24 6l1.5 12" stroke="S" stroke-width="2"/>
<path d="M21 5h6" stroke="n" stroke-width="2"/>
<circle cx="27.5" cy="9" r="1.4" fill="y"/>
<path d="M3 17c0-3 2.5-4.5 6-4.5h3l2.5 4h7.5l2-6 3 1.5 1 9.5H3z" fill="M"/>
<rect x="4" y="10" width="9" height="3" rx="1.5" fill="D"/>
<circle cx="8" cy="24.5" r="3.6" fill="n"/><circle cx="8" cy="24.5" r="1.5" fill="s"/><circle cx="24" cy="24.5" r="3.6" fill="n"/><circle cx="24" cy="24.5" r="1.5" fill="s"/>
""")

icon("bike", """
<circle cx="8" cy="21" r="6" stroke="n" stroke-width="2" fill="none"/><circle cx="24" cy="21" r="6" stroke="n" stroke-width="2" fill="none"/>
<path d="M8 21l5-9h9l2 9M13 12l4 9h-9M22 12l-1.5-4h-3" stroke="r" stroke-width="2" fill="none"/>
<path d="M10.5 9.5h5" stroke="D" stroke-width="2"/>
<circle cx="17" cy="21" r="1.4" fill="S"/>
""")

icon("car", """
<path d="M8.5 9h15l4 7.5H4.5z" fill="b"/>
<path d="M16 9v7.5" stroke="r" stroke-width="2"/>
<rect x="2.5" y="15" width="27" height="9.5" rx="3" fill="r"/>
<rect x="3" y="18" width="3" height="2.5" rx=".6" fill="y"/><rect x="26" y="18" width="3" height="2.5" rx=".6" fill="O"/>
""" + WHEELS)

icon("taxi", """
<rect x="12" y="4" width="8" height="4" rx="1" fill="n"/><path d="M14 6h4" stroke="y" stroke-width="1"/>
<path d="M8.5 9h15l4 7.5H4.5z" fill="b"/>
<path d="M16 9v7.5" stroke="y" stroke-width="2"/>
<rect x="2.5" y="15" width="27" height="9.5" rx="3" fill="y"/>
<path d="M3 20h26" stroke="n" stroke-width="1.6" stroke-dasharray="2 2"/>
<rect x="3" y="17" width="3" height="2" rx=".6" fill="W"/><rect x="26" y="17" width="3" height="2" rx=".6" fill="O"/>
""" + WHEELS)

icon("bus", """
<rect x="4" y="3" width="24" height="23" rx="3" fill="g"/>
<rect x="6.5" y="6" width="19" height="9" rx="1.5" fill="b"/>
<path d="M16 6v9" stroke="g" stroke-width="1.2"/>
<rect x="4" y="16.5" width="24" height="2" fill="G"/>
<circle cx="8.5" cy="21.5" r="1.8" fill="y"/><circle cx="23.5" cy="21.5" r="1.8" fill="y"/>
<rect x="12" y="20.5" width="8" height="2" rx="1" fill="n"/>
<rect x="6" y="26" width="4" height="3" rx="1" fill="n"/><rect x="22" y="26" width="4" height="3" rx="1" fill="n"/>
""")

icon("train", """
<rect x="6" y="3" width="20" height="21" rx="5" fill="l"/>
<rect x="9" y="7" width="14" height="8" rx="1.5" fill="b"/>
<rect x="6" y="16.5" width="20" height="2" fill="L"/>
<circle cx="10.5" cy="21" r="1.6" fill="y"/><circle cx="21.5" cy="21" r="1.6" fill="y"/>
<path d="M10 24l-4 5M22 24l4 5M4 29.5h24" stroke="n" stroke-width="1.8"/>
""")

icon("plane", """
<path d="M14 14l-4 14h3.5l7-13z" fill="b"/>
<path d="M14 14.5L10 4h3.5l7 10z" fill="B"/>
<rect x="2.5" y="12.5" width="27.5" height="6" rx="3" fill="W"/>
<path d="M3 12.5l-1-6h3l4 6z" fill="r"/>
<circle cx="11" cy="15.5" r=".9" fill="B"/><circle cx="15" cy="15.5" r=".9" fill="B"/><circle cx="19" cy="15.5" r=".9" fill="B"/>
<path d="M26.5 14.5h2" stroke="B" stroke-width="1.2"/>
""")

icon("ship", """
<rect x="13" y="4" width="4" height="9" fill="r"/><rect x="13" y="4" width="4" height="2" fill="n"/>
<rect x="8" y="11" width="16" height="6" rx="1" fill="W"/>
<circle cx="12" cy="14" r="1" fill="b"/><circle cx="16" cy="14" r="1" fill="b"/><circle cx="20" cy="14" r="1" fill="b"/>
<path d="M3 17h26l-4 8H7z" fill="B"/>
<path d="M3.5 18h25" stroke="r" stroke-width="2"/>
<path d="M2 27c2-1.5 4-1.5 6 0s4 1.5 6 0 4-1.5 6 0 4 1.5 6 0 4-1.5 4-1.5" stroke="b" stroke-width="2" fill="none"/>
""")

icon("fuel", """
<rect x="4" y="3.5" width="15" height="24" rx="2" fill="r"/>
<rect x="7" y="6.5" width="9" height="6" rx="1" fill="W"/>
<path d="M9 9.5h5" stroke="n" stroke-width="1.2"/>
<rect x="2.5" y="26.5" width="18" height="2.5" rx="1" fill="n"/>
<path d="M19 9h3.5l2 2.5V22a2 2 0 01-4 0v-4" stroke="n" stroke-width="1.8" fill="none"/>
<rect x="22.5" y="5" width="4" height="5" rx="1" fill="S"/>
""")

icon("parking", """
<rect x="14.5" y="20" width="3" height="9" fill="S"/>
<rect x="4" y="3" width="24" height="19" rx="3" fill="B"/>
<path d="M12.5 18V7h4.5a3.4 3.4 0 010 6.8h-4.5" stroke="W" stroke-width="2.6" fill="none"/>
""")

icon("road", """
<path d="M3 28V8.5c3 0 5 .5 7 1.5L4 28z" fill="g"/><path d="M29 28V8.5c-3 0-5 .5-7 1.5l6 18z" fill="g"/>
<path d="M12 4h8l8 24H4z" fill="S"/>
<path d="M16 6v3M16 12.5v4M16 20v6" stroke="y" stroke-width="1.6"/>
""")

icon("map-pin", """
<ellipse cx="16" cy="28" rx="7" ry="1.8" fill="s"/>
<path d="M16 28c-5-6-9-10.5-9-15a9 9 0 0118 0c0 4.5-4 9-9 15z" fill="r"/>
<circle cx="16" cy="13" r="3.4" fill="W"/>
""")

icon("compass", """
<circle cx="16" cy="16" r="12.5" fill="y"/>
<circle cx="16" cy="16" r="10" fill="W"/>
<path d="M16 7.5l3 8.5h-6z" fill="r"/><path d="M16 24.5l3-8.5h-6z" fill="B"/>
<circle cx="16" cy="16" r="1.4" fill="y"/>
""")

icon("luggage", """
<path d="M12 7V4.5a1 1 0 011-1h6a1 1 0 011 1V7" stroke="n" stroke-width="2" fill="none"/>
<rect x="6" y="7" width="20" height="20" rx="2.5" fill="O"/>
<rect x="10.5" y="7" width="2" height="20" fill="Y"/><rect x="19.5" y="7" width="2" height="20" fill="Y"/>
<circle cx="15.8" cy="12.5" r="2" fill="b"/><rect x="14" y="19" width="4.5" height="3.5" rx=".8" fill="g"/>
<circle cx="10" cy="28.5" r="1.4" fill="n"/><circle cx="22" cy="28.5" r="1.4" fill="n"/>
""")

icon("ticket", """
<path d="M3 8h26v5.5a2.5 2.5 0 000 5V24H3v-5.5a2.5 2.5 0 000-5z" fill="y"/>
<rect x="3" y="8" width="7" height="16" fill="r"/>
<path d="M10.5 10v12" stroke="Y" stroke-width="1" stroke-dasharray="1.5 1.5"/>
<path d="M19.5 11.5l1.4 2.9 3.2.4-2.3 2.2.6 3.2-2.9-1.6-2.9 1.6.6-3.2-2.3-2.2 3.2-.4z" fill="r"/>
""")

icon("tent", """
<path d="M16 4l13 23H3z" fill="O"/>
<path d="M16 4l13 23H16z" fill="r"/>
<path d="M16 12l5 15H11z" fill="D"/>
<path d="M16 4V1.5" stroke="C" stroke-width="1.4"/><path d="M16.5 1.5h4l-1 1.5 1 1.5h-4z" fill="y"/>
<rect x="2" y="27" width="28" height="2" rx="1" fill="g"/>
""")

icon("mountain", """
<circle cx="24.5" cy="7.5" r="3.5" fill="y"/>
<path d="M19 11l10 17H11z" fill="l"/>
<path d="M11 5l12 23H0z" fill="S"/>
<path d="M11 5l3.6 6.9-2-1-1.6 1.4-1.6-1.4-2 1z" fill="W"/>
<rect x="1.5" y="27" width="29" height="2" rx="1" fill="g"/>
""")
