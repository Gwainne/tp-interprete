// FizzBuzz del 1 al 20 usando repeat-until y módulo
int i = 1;

repeat {
    if (i % 15 == 0) {
        print("FizzBuzz");
    } else {
        if (i % 3 == 0) {
            print("Fizz");
        } else {
            if (i % 5 == 0) {
                print("Buzz");
            } else {
                print(i);
            }
        }
    }

    i = i + 1;
} until (i > 20);