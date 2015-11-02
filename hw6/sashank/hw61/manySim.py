#!/usr/bin/python
import sys
import os

for x in range(4, 11):
	os.system("java HW6 -preg 2048 -rob "+str(2**x)+" -width 8")
