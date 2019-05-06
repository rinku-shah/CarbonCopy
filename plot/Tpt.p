#!/bin/bash
set autoscale #scales axes automatically
unset log #remove any log-scaling
unset label #remove any prev labels
set terminal postscript eps enhanced color font 'Arial,15'
set output 'Tpt.eps'
#set xtic 3
set ytic auto
set xlabel "State Replication Design"
set ylabel "Average Throughput(requests/sec)"

set style fill pattern 4
set style data histogram
set style histogram gap 2

set size 1,0.5
#set key at 2,44
#set xrange [0:]
set yrange[0:1000]

plot newhistogram fs pattern 1, 'design.dat' using 2:xtic(1) title '6% writes' lc rgb 'red' lw 2  ,\
  'design.dat' using 5 title '25% writes' lc rgb 'black' lw 2  , \
 'design.dat' using 8 title '35% writes' lc rgb 'blue' lw 2, \
 'design.dat' using 11 title '50% writes' lc rgb 'purple' lw 2

unset style data
unset style histogram
