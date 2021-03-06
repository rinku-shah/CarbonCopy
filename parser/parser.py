#!/usr/bin/env python3

########################################################
# 1. Convert ip4 format to p4
########################################################

import sys
import os
import re
import json
import fileinput
from collections import OrderedDict
from pyparsing import Word, alphas, nums, nestedExpr, Keyword, alphanums, Regex, White, Optional

from util import *

class p4_code_generator():

    def __init__(self, src, dest,dest2, filename,folder):
        self.src = src
        self.primary_write_async = dest + ".writeasync"
        self.secondary_write_async = dest2 + ".writeasync"
        self.primary_write_sync = dest + ".writesync"
        self.secondary_write_sync = dest2 + ".writesync"
        self.primary = dest
        self.FOLDER = folder
        self.secondary = dest2
        self.tempfiles = [
            self.primary_write_async,
            self.secondary_write_async,
            self.primary_write_sync,
            self.secondary_write_sync
            ]

    def expand(self):
        self.expand_write_async()
        self.expand_write_sync()
        self.add_apply_async()
        self.add_apply_sync()
        for f in self.tempfiles:
            os.system('rm -f %s' % f)

    def expand_write_async(self):
        sfile = open(self.src,'r')
        dfile = open(self.primary_write_async,'w')
        d2file = open(self.secondary_write_async,'w')

        # Loop through ip4 source
        for row in sfile:

            # Collect CONSTANTS
            if KEYWORDS['write_async'] in row:
                indent = row[:-len(row.lstrip())]

                temp = open(self.FOLDER + "/" + "primary_write.txt",'r').read()
                temp = temp.replace("\n","\n" + indent)
                dfile.write(indent + temp)

                temp = open(self.FOLDER + "/" + "secondary_write.txt",'r').read()
                temp = temp.replace("\n","\n" + indent)
                d2file.write(indent + temp)

            else:
                dfile.write(row)
                d2file.write(row)

        sfile.close()
        dfile.close()

    def expand_write_sync(self):
        sfile = open(self.src,'r')
        dfile = open(self.primary_write_sync,'w')
        d2file = open(self.secondary_write_sync,'w')

        # Loop through ip4 source
        for row in sfile:

            # Collect CONSTANTS
            if KEYWORDS['write_sync'] in row:
                indent = row[:-len(row.lstrip())]

                temp = open(self.FOLDER + "/" + "primary_write.txt",'r').read()
                temp = temp.replace("\n","\n" + indent)
                dfile.write(indent + temp)

                temp = open(self.FOLDER + "/" + "secondary_write.txt",'r').read()
                temp = temp.replace("\n","\n" + indent)
                d2file.write(indent + temp)

            else:
                dfile.write(row)
                d2file.write(row)

        sfile.close()
        dfile.close()


    def add_apply_async(self):
        sfile = open(self.primary_write_async,'r')
        s2file = open(self.secondary_write_async,'r')
        dfile = open(self.primary,'w')
        d2file = open(self.secondary,'w')
        foundIngress = False;
        foundEgress = False;
        current = 0
        # Loop through ip4 source
        for row in sfile:
            # Collect CONSTANTS
            if foundIngress:
                temp = open(self.FOLDER + "/" + "ingress_apply.txt",'r').read()
                indent = row[:-len(row.lstrip())]
                temp = temp.replace("\n","\n" + indent)
                dfile.write(indent + temp)
                foundIngress = False
            if foundEgress:
                temp = open(self.FOLDER + "/" + "egress_apply.txt",'r').read()
                indent = row[:-len(row.lstrip())]
                temp = temp.replace("\n","\n" + indent)
                dfile.write(indent + temp)
                foundEgress = False
            if KEYWORDS['apply'] in row:
                if current == 0:
                    current = 1
                    foundIngress = True
                elif current == 1:
                    foundEgress = True
                    current = 2
                dfile.write(row)
            else:
                dfile.write(row)
        sfile.close()
        dfile.close()

        foundIngress = False;
        current = 0
        for row in s2file:
            if foundIngress:
                temp = open(self.FOLDER + "/" + "ingress_apply.txt",'r').read()
                indent = row[:-len(row.lstrip())]
                temp = temp.replace("\n","\n" + indent)
                d2file.write(indent + temp)
                foundIngress = False
            if KEYWORDS['apply'] in row:
                if current == 0:
                    current = 1
                    foundIngress = True
                elif current == 1:
                    foundEgress = True
                    current = 2
                d2file.write(row)
            else:
                d2file.write(row)

        s2file.close()
        d2file.close()



    def add_apply_sync(self):
        sfile = open(self.primary_write_sync,'r')
        s2file = open(self.secondary_write_sync,'r')
        dfile = open(self.primary,'w')
        d2file = open(self.secondary,'w')
        foundIngress = False;
        foundEgress = False;
        current = 0
        # Loop through ip4 source
        for row in sfile:
            # Collect CONSTANTS
            if foundIngress:
                temp = open(self.FOLDER + "/" + "ingress_apply_sync.txt",'r').read()
                indent = row[:-len(row.lstrip())]
                temp = temp.replace("\n","\n" + indent)
                dfile.write(indent + temp)
                foundIngress = False
            if foundEgress:
                temp = open(self.FOLDER + "/" + "egress_apply.txt",'r').read()
                indent = row[:-len(row.lstrip())]
                temp = temp.replace("\n","\n" + indent)
                dfile.write(indent + temp)
                foundEgress = False
            if KEYWORDS['apply'] in row:
                if current == 0:
                    current = 1
                    foundIngress = True
                elif current == 1:
                    foundEgress = True
                    current = 2
                dfile.write(row)
            else:
                dfile.write(row)
        sfile.close()
        dfile.close()

        foundIngress = False;
        current = 0
        for row in s2file:
            if foundIngress:
                temp = open(self.FOLDER + "/" + "ingress_apply.txt",'r').read()
                indent = row[:-len(row.lstrip())]
                temp = temp.replace("\n","\n" + indent)
                d2file.write(indent + temp)
                foundIngress = False
            if KEYWORDS['apply'] in row:
                if current == 0:
                    current = 1
                    foundIngress = True
                elif current == 1:
                    foundEgress = True
                    current = 2
                d2file.write(row)
            else:
                d2file.write(row)

        s2file.close()
        d2file.close()




if __name__ == '__main__':
    if len(sys.argv) != 2:
        print("Format: python3 %s <filename>.p4" % sys.argv[0])
        sys.exit()

    src = sys.argv[1]
    ip4_file = src.split('/')[-1]
    filename = ip4_file.split('.')[0]

    print(src,ip4_file, filename)
    dest = "primary.p4"
    dest2 = "secondary.p4"
    folder = "files"
    code_gen = p4_code_generator(src,dest,dest2,filename,folder)
    code_gen.expand()

    # generate_sync_header(p4src_folder, filename)
