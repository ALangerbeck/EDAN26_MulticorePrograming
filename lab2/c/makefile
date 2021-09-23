main:
	gcc -o preflow preflow.c -g -O3 -pthread -fsanitize=thread
	time sh check-solution.sh ./preflow
	@echo PASS all tests
