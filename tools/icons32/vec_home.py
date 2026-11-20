# Nhóm NHÀ CỬA – vector 32×32

HOUSE = """
<rect x="21" y="5" width="3.5" height="7" fill="C"/>
<rect x="6" y="14" width="20" height="14" fill="{wall}"/>
<path d="M2.5 15.5L16 4l13.5 11.5" stroke="{roof}" stroke-width="3.4" fill="none"/>
<path d="M6 14L16 5.5 26 14z" fill="{roof2}"/>
<rect x="3" y="27.5" width="26" height="2" rx="1" fill="g"/>
"""

icon("home", HOUSE.format(wall="w", roof="r", roof2="P") + """
<rect x="13" y="19" width="6" height="9" rx=".8" fill="C"/>
<circle cx="17.3" cy="23.5" r=".8" fill="y"/>
<rect x="7.5" y="17.5" width="4" height="4" rx=".6" fill="b"/><rect x="20.5" y="17.5" width="4" height="4" rx=".6" fill="b"/>
""")

icon("home-heart", HOUSE.format(wall="W", roof="P", roof2="p") + """
<path d="M16 25c-3.6-2.4-5.4-4.2-5.4-6a2.7 2.7 0 015.4-.8 2.7 2.7 0 015.4.8c0 1.8-1.8 3.6-5.4 6z" fill="r"/>
""")

icon("building", """
<rect x="3" y="12" width="10" height="17" fill="b"/>
<rect x="11" y="3" width="17" height="26" fill="S"/>
<rect x="14" y="6" width="3" height="3" fill="y"/><rect x="19" y="6" width="3" height="3" fill="b"/><rect x="24" y="6" width="2" height="3" fill="y"/>
<rect x="14" y="12" width="3" height="3" fill="b"/><rect x="19" y="12" width="3" height="3" fill="y"/><rect x="24" y="12" width="2" height="3" fill="b"/>
<rect x="14" y="18" width="3" height="3" fill="y"/><rect x="19" y="18" width="3" height="3" fill="y"/><rect x="24" y="18" width="2" height="3" fill="b"/>
<rect x="5" y="15" width="2.5" height="2.5" fill="y"/><rect x="5" y="20" width="2.5" height="2.5" fill="y"/>
<rect x="17" y="24" width="5" height="5" fill="D"/>
""")

icon("droplet", """
<path d="M16 3c4.5 6 9 11 9 16a9 9 0 01-18 0c0-5 4.5-10 9-16z" fill="b"/>
<path d="M11.5 19.5c0 2.6 1.6 4.6 4 5.4" stroke="W" stroke-width="1.6" fill="none"/>
""")

icon("wifi", """
<path d="M3.5 13a17.5 17.5 0 0125 0" stroke="t" stroke-width="3" fill="none"/>
<path d="M8 18a11 11 0 0116 0" stroke="M" stroke-width="3" fill="none"/>
<path d="M12.3 22.6a5 5 0 017.4 0" stroke="b" stroke-width="3" fill="none"/>
<circle cx="16" cy="26.5" r="2.2" fill="t"/>
""")

icon("tv", """
<path d="M16 9.5L10 3.5M16 9.5l6-6" stroke="n" stroke-width="1.4"/>
<circle cx="10" cy="3.5" r="1.4" fill="y"/><circle cx="22" cy="3.5" r="1.4" fill="y"/>
<rect x="3" y="9" width="26" height="18" rx="3" fill="p"/>
<rect x="6" y="12" width="16" height="12" rx="2" fill="t"/>
<circle cx="25.5" cy="15" r="1.6" fill="W"/><circle cx="25.5" cy="20.5" r="1.6" fill="W"/>
<rect x="7" y="27" width="3" height="2" fill="n"/><rect x="22" y="27" width="3" height="2" fill="n"/>
""")

icon("bulb", """
<path d="M16 3a9 9 0 00-5 16.5V22h10v-2.5A9 9 0 0016 3z" fill="y"/>
<path d="M13 13.5l3 3 3-3M16 16.5V21" stroke="O" stroke-width="1.1" fill="none"/>
<rect x="11" y="21.5" width="10" height="5" rx="1" fill="s"/>
<path d="M11 24h10" stroke="S" stroke-width="1"/>
<rect x="13.5" y="26.5" width="5" height="2.5" rx="1" fill="n"/>
""")

icon("bed", """
<rect x="2.5" y="7" width="4" height="22" rx="1" fill="C"/>
<rect x="6" y="17" width="23.5" height="7" fill="W"/>
<rect x="7.5" y="13" width="7" height="4.5" rx="2" fill="W"/>
<rect x="14" y="13.5" width="15.5" height="8" rx="1.5" fill="b"/>
<path d="M14.5 17.5h14.5" stroke="B" stroke-width="1"/>
<rect x="6" y="23.5" width="23.5" height="2.5" fill="c"/>
<rect x="26" y="25.5" width="3.5" height="3.5" rx=".8" fill="C"/>
""")

icon("bath", """
<path d="M6 16V7a2.5 2.5 0 015 0" stroke="S" stroke-width="2" fill="none"/>
<circle cx="12.5" cy="13" r="3" fill="W"/><circle cx="18" cy="12" r="3.6" fill="W"/><circle cx="23.5" cy="13.5" r="2.6" fill="W"/>
<rect x="2.5" y="15" width="27" height="3.5" rx="1.5" fill="W"/>
<path d="M4 18h24v3a5 5 0 01-5 5H9a5 5 0 01-5-5z" fill="W"/>
<path d="M4.5 18.5h23" stroke="b" stroke-width="1.4"/>
<path d="M7.5 25.5l-1 3.5M24.5 25.5l1 3.5" stroke="y" stroke-width="2"/>
""")

icon("sofa", """
<rect x="6" y="7" width="20" height="12" rx="3" fill="t"/>
<path d="M16 8v10" stroke="T" stroke-width="1"/>
<rect x="2.5" y="13" width="6" height="13" rx="2.5" fill="t"/><rect x="23.5" y="13" width="6" height="13" rx="2.5" fill="t"/>
<rect x="7" y="18" width="18" height="6.5" rx="1.5" fill="M"/>
<path d="M16 18.5v5.5" stroke="t" stroke-width="1"/>
<rect x="10" y="10.5" width="5" height="5" rx="1.5" fill="p"/>
<rect x="4" y="26" width="2.5" height="3" fill="D"/><rect x="25.5" y="26" width="2.5" height="3" fill="D"/>
""")

icon("fan", """
<rect x="15" y="17" width="2" height="9" fill="S"/>
<ellipse cx="16" cy="27" rx="7" ry="2.2" fill="W"/>
<circle cx="16" cy="11" r="9.5" fill="W"/>
<ellipse cx="16" cy="6.8" rx="2.6" ry="4" fill="b"/><ellipse cx="20" cy="13" rx="4" ry="2.6" fill="b" transform="rotate(30 20 13)"/><ellipse cx="12" cy="13" rx="4" ry="2.6" fill="b" transform="rotate(-30 12 13)"/>
<circle cx="16" cy="11" r="2" fill="B"/>
""")

icon("plug", """
<rect x="10.5" y="3" width="2.5" height="7" rx="1" fill="s"/><rect x="19" y="3" width="2.5" height="7" rx="1" fill="s"/>
<rect x="7" y="9" width="18" height="10" rx="2.5" fill="W"/>
<path d="M9 19h14l-3 5h-8z" fill="W"/>
<circle cx="16" cy="14" r="1.2" fill="g"/>
<path d="M16 24v5" stroke="n" stroke-width="2.4"/>
""")

icon("flame", """
<path d="M16 3c3 5 9 8 9 15a9 9 0 01-18 0c0-4 2-6 4-8 0 3 1.5 4 3 4-1-5 0-8 2-11z" fill="r"/>
<path d="M16 12c2 3.5 5.5 5.5 5.5 10a5.5 5.5 0 01-11 0c0-2.5 1.2-4 2.5-5 .3 1.6 1 2.4 2 2.4-.5-3 0-5 1-7.4z" fill="O"/>
<path d="M16 19.5c1 1.8 2.6 3 2.6 5a2.6 2.6 0 01-5.2 0c0-1.8 1.4-3 2.6-5z" fill="y"/>
""")

icon("key", """
<circle cx="10" cy="16" r="7" fill="y"/>
<circle cx="8.5" cy="16" r="2.2" fill="W"/>
<rect x="15.5" y="14.2" width="13.5" height="3.6" rx="1" fill="y"/>
<rect x="21" y="17" width="2.5" height="4.5" fill="y"/><rect x="25.5" y="17" width="2.5" height="5.5" fill="y"/>
""")

icon("key-house", """
<path d="M3 12L12 4l9 8" stroke="r" stroke-width="3" fill="none"/>
<rect x="5.5" y="11" width="13" height="10" fill="w"/>
<rect x="10" y="14.5" width="4" height="6.5" fill="C"/>
<circle cx="23" cy="20" r="5" fill="y"/><circle cx="23" cy="20" r="1.8" fill="W"/>
<path d="M19.5 23.5l-9 6" stroke="y" stroke-width="2.6"/>
<path d="M13 27.8l1.6 2.2" stroke="y" stroke-width="2"/>
""")

icon("lock", """
<path d="M10 14V9.5a6 6 0 0112 0V14" stroke="s" stroke-width="3" fill="none"/>
<rect x="5.5" y="13" width="21" height="16" rx="2.5" fill="y"/>
<circle cx="16" cy="19.5" r="2.2" fill="D"/><rect x="15" y="20" width="2" height="4.5" rx=".8" fill="D"/>
""")

icon("tree", """
<rect x="14" y="19" width="4" height="10" fill="C"/>
<circle cx="16" cy="12" r="9" fill="g"/>
<circle cx="10.5" cy="15" r="5" fill="G"/><circle cx="21.5" cy="15" r="5" fill="G"/><circle cx="16" cy="9" r="6" fill="g"/>
<circle cx="11" cy="10" r="1.3" fill="r"/><circle cx="21" cy="8.5" r="1.3" fill="r"/><circle cx="18.5" cy="15.5" r="1.3" fill="r"/>
<rect x="8" y="28" width="16" height="1.6" rx=".8" fill="G"/>
""")

icon("sprout", """
<path d="M16 19V10" stroke="G" stroke-width="2"/>
<path d="M16 12c-1-4-5-6-10-5 1 4 5 6 10 5z" fill="g"/>
<path d="M16 10c1-4 5-6 10-5-1 4-5 6-10 5z" fill="M"/>
<path d="M6 18h20l-2.5 11h-15z" fill="O"/>
<rect x="5" y="16.5" width="22" height="4" rx="1" fill="C"/>
""")

icon("flower", """
<path d="M16 17v12" stroke="G" stroke-width="2"/>
<path d="M16 25c-2-3-5.5-3.5-8-2.5 2 3 5.5 3.5 8 2.5zM16 22c2-3 5.5-3.5 8-2.5-2 3-5.5 3.5-8 2.5z" fill="g"/>
<circle cx="16" cy="5.5" r="4" fill="p"/><circle cx="21.7" cy="9.5" r="4" fill="p"/><circle cx="19.6" cy="16" r="4" fill="p"/><circle cx="12.4" cy="16" r="4" fill="p"/><circle cx="10.3" cy="9.5" r="4" fill="p"/>
<circle cx="16" cy="11" r="3.6" fill="y"/>
""")

icon("palm", """
<path d="M3 29c2-4 7-6 13-6s11 2 13 6z" fill="c"/>
<path d="M17 24c0-6-1-11-3-15" stroke="C" stroke-width="3" fill="none"/>
<path d="M14 9c-3-3-8-3-11 0 3 .5 6 1 11 0zM14 9c-1-4-5-6-9-6 1 3 4 6 9 6zM14 9c2-3.5 6-5 10-4-1.5 3-5 4.5-10 4zM14 9c3-2 8-2 11 1-3.5 1-7 1-11-1z" fill="g"/>
<circle cx="13" cy="11" r="1.6" fill="D"/><circle cx="16" cy="11" r="1.6" fill="D"/>
""")
