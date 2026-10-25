# The command shell. Copy it to your own shell.py to customise it.

HELP = """Commands:
  ls                list your files
  programs          list programs you can run
  edit <file>       edit a file, e.g. edit hello.py
  cat <file>        show a file
  rm <file>         delete a file
  cp <a> <b>        copy a file  (mv moves it)
  <program> [args]  run a program, e.g. devices
  python            interactive Python
  label [name]      show or set this computer's name
  id | clear | reboot | shutdown
Terminate (Ctrl+T) stops a running program."""


def prompt():
    return (computer.label() or computer.id()) + '> '


def repl():
    print "Python 2.7 (Jython). Type exit to leave."
    env = {'__name__': '__console__'}
    lines = []
    while True:
        line = raw_input('... ' if lines else '>>> ')
        if not lines and line.strip() == 'exit':
            return
        if lines or line.rstrip().endswith(':'):
            if line.strip():
                lines.append(line)
                continue
            source, lines = '\n'.join(lines), []
        else:
            source = line
        if not source.strip():
            continue
        try:
            try:
                code = compile(source, '<console>', 'eval')
            except SyntaxError:
                exec compile(source + '\n', '<console>', 'exec') in env
            else:
                result = eval(code, env)
                if result is not None:
                    print repr(result)
        except KeyboardInterrupt:
            print 'Terminated.'
        except Exception:
            print computer.traceback()


def command(cmd, rest):
    if cmd == 'help':
        print HELP
    elif cmd == 'ls':
        files = fs.list()
        print '  '.join(files) if files else '(no files yet - try: edit hello.py)'
    elif cmd == 'programs':
        print 'Built in: ' + ', '.join(fs.list('rom'))
        mine = [f[:-3] for f in fs.list() if f.endswith('.py')]
        if mine:
            print 'Yours:    ' + ', '.join(mine)
    elif cmd == 'edit' and rest:
        term.edit(rest[0])
    elif cmd == 'cat' and rest:
        text = fs.read(rest[0])
        print text if text is not None else 'No such file.'
    elif cmd == 'rm' and rest:
        print 'Deleted.' if fs.delete(rest[0]) else 'No such file.'
    elif cmd in ('cp', 'mv') and len(rest) == 2:
        text = fs.read(rest[0])
        if text is None:
            print 'No such file.'
        else:
            fs.write(rest[1], text)
            if cmd == 'mv':
                fs.delete(rest[0])
    elif cmd == 'python':
        repl()
    elif cmd == 'label':
        if rest:
            computer.set_label(' '.join(rest))
        print computer.label() or '(no label)'
    elif cmd == 'id':
        print computer.id()
    elif cmd == 'clear':
        term.clear()
    elif cmd == 'reboot':
        computer.reboot()
    elif cmd == 'shutdown':
        computer.shutdown()
    elif not computer.run(cmd, rest):
        print "Unknown command '%s'. Type help." % cmd


print 'Triassic Island OS 1.0'
print "Type help for commands, programs for apps."
while True:
    try:
        line = raw_input(prompt())
    except KeyboardInterrupt:
        continue
    parts = line.split()
    if not parts:
        continue
    try:
        command(parts[0], parts[1:])
    except KeyboardInterrupt:
        print 'Terminated.'
    except Exception:
        print computer.traceback()
