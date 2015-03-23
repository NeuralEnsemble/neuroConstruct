#! /usr/bin/env python

"""
    start socket based minimal readline exec server
"""
# this part of the program only executes on the server side
#

progname = 'socket_readline_exec_server-1.2'

import sys, socket, os
from time import sleep
try:
    import fcntl
except ImportError:
    fcntl = None

FORCE_START = True # If the port is in use, end whatever processing is using it first.  
debug = 0

if debug: #  and not os.isatty(sys.stdin.fileno()):
    f = open('/tmp/execnet-socket-pyout.log', 'w')
    old = sys.stdout, sys.stderr
    sys.stdout = sys.stderr = f

def print_(*args):
    print(" ".join(str(arg) for arg in args))

if sys.version_info > (3, 0):
    exec("""def exec_(source, locs):
    exec(source, locs)""")
else:
    exec("""def exec_(source, locs):
    exec source in locs""")

def exec_from_one_connection(serversock):
    print_(progname, 'Entering Accept loop', serversock.getsockname())
    clientsock,address = serversock.accept()
    print_(progname, 'got new connection from %s %s' % address)
    clientfile = clientsock.makefile('rb')
    print_("reading line")
    # rstrip so that we can use \r\n for telnet testing
    source = clientfile.readline().rstrip()
    clientfile.close()
    g = {'clientsock' : clientsock, 'address' : address}
    source = eval(source)
    if source:
        co = compile(source+'\n', source, 'exec')
        print_(progname, 'compiled source, executing')
        try:
            exec_(co, g)
        finally:
            print_(progname, 'finished executing code')
            # background thread might hold a reference to this (!?)
            #clientsock.close()

def bind_and_listen(hostport):
    if isinstance(hostport, str):
        host, port = hostport.split(':')
        hostport = (host, int(port))
    tries = 0
    while tries < 2:
        try:
            serversock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            # set close-on-exec
            if hasattr(fcntl, 'FD_CLOEXEC'):
                old = fcntl.fcntl(serversock.fileno(), fcntl.F_GETFD)
                fcntl.fcntl(serversock.fileno(), fcntl.F_SETFD, old | fcntl.FD_CLOEXEC)
            # allow the address to be re-used in a reasonable amount of time
            if os.name in ['posix','java'] and sys.platform != 'cygwin':
                print "Enabling address reuse."
                serversock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            serversock.bind(hostport)
            serversock.listen(5)
        except Exception,e:
            if FORCE_START:
                print 'Forcing shutdown of existing socket.'
                print tries
                try:
                    serversock.shutdown(0)
                except:
                    pass
                if tries == 1:
                    raise e
                serversock.close()
                sleep(10)
                tries += 1
            else:
                raise e
        else:
            break
    return serversock

def startserver(serversock, loop=False):
    try:
        while 1:
            try:
                exec_from_one_connection(serversock)
            except (KeyboardInterrupt, SystemExit):
                raise
            except:
                if debug:
                    import traceback
                    traceback.print_exc()
                else:
                    excinfo = sys.exc_info()
                    print_("got exception", excinfo[1])
            if not loop:
                break
    finally:
        print_("leaving socketserver execloop")
        try:
            serversock.shutdown(0)
            serversock.close()
        except:
            pass

if __name__ == '__main__':
    import sys
    if len(sys.argv)>1:
        hostport = sys.argv[1]
    else:
        hostport = ':8888'
    serversock = bind_and_listen(hostport)
    startserver(serversock, loop=False)

