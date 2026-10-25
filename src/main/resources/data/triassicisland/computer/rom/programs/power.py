# power - live view of the power grid. Terminate (Ctrl+T) to exit.

KINDS = ('combustion_generator', 'solar_panel', 'wind_turbine', 'battery',
         'large_battery', 'transformer', 'circuit_breaker')

while True:
    rows = []
    for d in net.devices():
        if d['type'] not in KINDS:
            continue
        dev = net.wrap(d['id'])
        name = (d['label'] or d['id'])[:24]
        extra = ''
        if d['type'] in ('battery', 'large_battery'):
            extra = '%3.0f%%' % dev.charge()
        elif d['type'] == 'circuit_breaker':
            extra = 'OPEN' if dev.is_open() else 'ok'
        elif d['type'] == 'transformer':
            extra = '%.0fV out' % dev.output_voltage()
        rows.append('%-24s %6.1fV %6.0fW %s' % (name, dev.voltage(), dev.power(), extra))
    term.clear()
    print 'POWER GRID                       Ctrl+T to exit'
    print '-' * 50
    for row in rows or ['No power devices on the network.']:
        print row
    computer.sleep(1)
