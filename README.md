# Kitteh2
Second revision of my Kitteh language. Now comes with a compiler to x86.

[Example programs](test/tests/) (used to test the compiler)

[Explanation of how the compiler works (1400 words + diagrams)](https://drive.google.com/open?id=0B80kPFdC2o1rSjF1QTcteEVMWkE)

[![Build Status](https://travis-ci.org/leijurv/Kitteh2.svg?branch=master)](https://travis-ci.org/leijurv/Kitteh2) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/0f4594175ed0407aa36d97c068f9ae9f)](https://www.codacy.com/app/leijurv/Kitteh2?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=leijurv/Kitteh2&amp;utm_campaign=Badge_Grade) [![codebeat badge](https://codebeat.co/badges/87580ebc-79ef-45b9-b316-aa7ad343cc27)](https://codebeat.co/projects/github-com-leijurv-kitteh2-master)

# Build
Kitteh2 is written in Java 8 and uses ant as a build system. The repo itself is also set up as a NetBeans project.
Building the compiler: 

```
ant jar
```

Compiling a program:

```
java -jar dist/Kitteh2.jar -i test/tests/fizzbuzz.k -o output.s
gcc -o executable output.s
./executable
```

The first and second steps can optionally be combined:

```
dist/k2c -i test/tests/mandelbrot.k -o executable -e
./executable
```

# Options
`-v` enable verbose mode. compiler will print a lot more information and outputted assembly will include helpful comments

`-i inputFile.k` set the input location

`-o output` set the output location

`-I` read input program from standard in

`-O` write output to standard out. not recommended except for debugging

`-d` enable deterministic builds. when this option is enabled, the compiler will produce consistent output when ran repeatedly on the same input

`-e` generate an executable file instead of the assembly as a string. this options simply runs gcc under the hood

`-obf` obfuscate labels and function names in output. compatible with `-d`
