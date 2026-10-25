# dinotracker - every dinosaur near this computer.
#   dinotracker          list them (nearest first)
#   dinotracker watch    keep updating (Ctrl+T to exit)
#   dinotracker <radius>

COMPASS = ['N', 'NE', 'E', 'SE', 'S', 'SW', 'W', 'NW']
here = computer.position()


def direction(d):
    import math
    dx, dz = d['x'] - here[0], d['z'] - here[2]
    angle = math.degrees(math.atan2(dx, -dz)) % 360
    return COMPASS[int((angle + 22.5) // 45) % 8]


def show(radius):
    dinos = sorted(computer.dinosaurs(radius), key=lambda d: d['distance'])
    print '%-18s %5s %5s %4s %5s %s' % ('DINOSAUR', 'HP', 'FOOD', 'AGE', 'DIST', 'DIR')
    for d in dinos[:16]:
        warn = ' HUNGRY' if d['food'] < 40 else ''
        print '%-18s %5.0f %4d%% %3.0f%% %4.0fm %-2s%s' % (d['name'][:18], d['health'], d['food'],
                                                     d['growth'] * 100, d['distance'], direction(d), warn)
    if not dinos:
        print 'No dinosaurs within %d blocks.' % radius
    elif len(dinos) > 16:
        print '... and %d more' % (len(dinos) - 16)


radius = 128
watch = False
for arg in args:
    if arg == 'watch':
        watch = True
    else:
        radius = int(arg)
if watch:
    while True:
        term.clear()
        show(radius)
        computer.sleep(2)
else:
    show(radius)
