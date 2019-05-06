#!/bin/bash
set autoscale #scales axes automatically
unset log #remove any log-scaling
unset label #remove any prev labels
set terminal postscript eps enhanced color font 'Arial,14'
set output 'FT-tpt.eps'
set xtic 20
set ytic auto
#set title "Adaptive mode: Throughput sampled every 10sec"
set xlabel "Time in secs"
set ylabel "Throughput(Requests per sec)"

#set style fill pattern 4
#set style data histogram
#set style histogram gap 2

#set key at 5,7500
#set xrange [10:]
set yrange[0:1000]
#set xtics border in scale 0,0 nomirror rotate by 45  offset character -1, -4, 0
set size 1,0.5
#set arrow from 600,0 to 600,500 nohead ls 3 lw 4
#set label "Fail" at 550,530 nopoint tc lt 3
#set arrow from 620,0 to 620,500 nohead ls 3 lw 4
#set label "Recover" at 620,530 nopoint tc lt 3

set xtics rotate by 90 offset -0.8,-1.8

plot 'basic-inst.dat' using 1:2 title 'Basic' with linespoints ls 1 lw 3 lc rgb 'red' , \
'CC-CP-inst.dat' using 1:2 title 'CC-CP' with linespoints ls 2 lw 3 lc rgb 'black' , \
'CC-sync-inst.dat' using 1:2 title 'CC-sync' with linespoints ls 3 lw 3 lc rgb 'blue' #, \
#'CC-async-inst.dat' using 1:2 title 'CC-async' with linespoints ls 4 lw 3 lc rgb 'purple' 
unset arrow
unset label
#unset style data
#unset style histogram
