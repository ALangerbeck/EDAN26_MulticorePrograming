main:
	gcc -o preflow preflowRelaxed.c pthread_barrier.c -g -O3 -pthread #-fsanitize=thread
	time sh check-solution.sh ./preflow
	@echo PASS all tests
