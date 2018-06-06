set term pdf
set output "load.pdf"
set log x
set ydata time
set timefmt "%s"
set format y "%h:%m"
set log y
set xlabel "#threads"
set ylabel "time (hour:min)"
plot "slowreader.dat" using ($1):($2/60) with linespoints title "Java standard IO library", \
	"fastreader.dat" using ($1):($2/60) with linespoints title "our implementation", \
	25 with dots title "pure data transfer on 1 single thread"