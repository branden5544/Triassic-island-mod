# Triassic Island OS - BIOS.
# Adds a few helpers to the built-in modules, runs startup.py if you have one, then starts the shell.


def _wrap(device_id):
    """net.wrap(id) -> an object whose methods call the device.
    Example: net.wrap('circuit_breaker_1').open()"""
    class Device(object):
        def __getattr__(self, name):
            if name.startswith('_'):
                raise AttributeError(name)

            def method(*args):
                return net.call(device_id, name, *args)
            return method

        def __repr__(self):
            return '<device %s>' % device_id
    return Device()


def _find(kind):
    """net.find(type_or_label) -> ids of matching devices, e.g. net.find('battery')"""
    return [d['id'] for d in net.devices() if kind in (d['type'], d['label'])]


def _run(name, args=()):
    """computer.run(name, args) - run a program: your file (name or name.py) first, then the built-in ones.
    Returns False if there's no such program."""
    for path in (name, name + '.py', 'rom/programs/' + name + '.py'):
        if fs.exists(path):
            source = fs.read(path)
            break
    else:
        return False
    code = compile(source.replace('\r\n', '\n') + '\n', path, 'exec')
    env = {'__name__': '__main__', 'args': list(args)}
    exec code in env
    return True


net.wrap = _wrap
net.find = _find
computer.run = _run

term.clear()
if fs.exists('startup.py'):
    try:
        _run('startup.py')
    except KeyboardInterrupt:
        print 'startup.py terminated.'
    except Exception:
        print computer.traceback()
_run('shell')
