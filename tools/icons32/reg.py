REG = {}
def icon(name):
    def deco(f):
        REG[name] = f
        return f
    return deco
