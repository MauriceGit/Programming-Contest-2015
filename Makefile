
.PHONY: all clean run name

all: 
	pkill java || echo " " > /dev/null 2>&1
	
clean:
	pkill java || echo " " > /dev/null 2>&1

run:
	pkill java || echo " " > /dev/null 2>&1
	java -jar samegame.jar	

name:
	echo "CluelessKI ... KI ... lol^^"

