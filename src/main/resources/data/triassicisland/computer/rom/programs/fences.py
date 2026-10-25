# fences - status of every fence gate and circuit breaker.
#   fences        show once
#   fences watch  keep updating (Ctrl+T to exit)

LIVE_VOLTS = 100


def show():
    gates = [d for d in net.devices() if d['type'] == 'electric_fence_gate']
    breakers = [d for d in net.devices() if d['type'] == 'circuit_breaker']
    dead = 0
    print 'FENCE GATES'
    for d in gates:
        g = net.wrap(d['id'])
        volts = g.voltage()
        live = volts >= LIVE_VOLTS
        if not live and not g.is_open():
            dead += 1
        print '  %-26s %-5s %4.0fV %s' % ((d['label'] or d['id'])[:26], 'LIVE' if live else 'DEAD',
                                         volts, 'open' if g.is_open() else 'closed')
    if not gates:
        print '  (none connected)'
    print 'BREAKERS'
    for d in breakers:
        b = net.wrap(d['id'])
        print '  %-26s %-6s %5.1f / %d A' % ((d['label'] or d['id'])[:26], 'OPEN' if b.is_open() else 'closed',
                                            b.amps(), b.rating())
    if not breakers:
        print '  (none connected)'
    if dead:
        print '!! %d closed gate(s) have no power!' % dead


if args and args[0] == 'watch':
    while True:
        term.clear()
        show()
        computer.sleep(2)
else:
    show()
