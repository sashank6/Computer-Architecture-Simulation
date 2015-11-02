#!/usr/bin/python
import sys
import os

for x in range(1, 7):
    os.system("java HW5 -width "+str(x)+" -bpred")
