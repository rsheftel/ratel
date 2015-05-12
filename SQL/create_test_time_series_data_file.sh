perl -F, -nae 'for (1..5) { $tsid = 0 + $_; print "$tsid\t1\t$F[0] 15:00:00 EST\t$F[$_]\n"; }' apple.csv > test_time_series_data.txt
perl -F, -nae 'for (1..5) { $tsid = 5 + $_; print "$tsid\t1\t$F[0] 15:00:00 EST\t$F[$_]\n"; }' sp500.csv >> test_time_series_data.txt

