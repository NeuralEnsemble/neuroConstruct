import os

# contents of: remotecmd.py

x = 2

def simple(arg):
    return arg + 1

def listdir(path):
    return os.listdir(path)
    
if __name__ == '__channelexec__':
    for item in channel:
        channel.send(eval(item))