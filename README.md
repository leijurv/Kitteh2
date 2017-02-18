# Kitteh2
Second revision of my Kitteh language. Now comes with a compiler to x86.

[Example programs](test/tests/) (used to test the compiler)

[Moderately disorganized and non-comprehensive spec](https://docs.google.com/document/d/1dw9Ag-AqB0_U6fwufkz3x9wXi0YkXPNi9wMsqXqoC-M/edit)

[![Build Status](https://travis-ci.org/leijurv/Kitteh2.svg?branch=master)](https://travis-ci.org/leijurv/Kitteh2)

# Build
Kitteh2 is written in Java 8 and uses ant as a build system. The repo itself is also set up as a NetBeans project.
Building the compiler: 

```
ant jar
```

Compiling a program:

```
java -jar dist/Kitteh2.jar -i inputProgram.k -o output.s
gcc -o executable output.s
./executable
```

The first and second steps can optionally be combined:

```
java -jar dist/Kitteh2.jar -i inputProgram.k -o executable -e
./executable
```

# Options
`-i inputFile.k`: set the input location
`-o output`: set the output location
`-I`: read input program from standard in
`-O`: write output to standard out. not recommended except for debugging
`-v`: enable verbose mode. compiler will print a lot more information and outputted assembly will include helpful comments
`-d`: enable deterministic builds. when this option is enabled, the compiler will produce consistent output when ran repeatedly on the same input
`-e`: generate an executable file instead of the assembly as a string. this options simply runs gcc under the hood
`-obf`: obfuscate labels and function names in output. compatible with `-d`
