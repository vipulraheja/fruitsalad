#!/bin/sh

for i in 2 3 5 10 15 30 100;
do
	java fruit.sim.Fruit players.list $i dist/RandomizedDistribution1.txt false false 1000 2> r$i
	java fruit.sim.Fruit players.list $i dist/uniform.txt false false 1000 2> u$i
done

for i in 2 3 5 10 15 30 100;
do
	echo "Random1 $i" >> result.txt
	tail -17 r$i  >> result.txt
	echo "" >> result.txt
	rm r$i

	echo "uniform $i" >> result.txt
	tail -17 u$i  >> result.txt
	echo "" >> result.txt
	rm u$i
done
