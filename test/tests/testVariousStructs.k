struct teststruct{
	int a;
;
	int b//lmao
	long* c ;; ;; ;; ;; ; ; ; ; ; ; ; ;  ;
}
main(){
	b:=(teststruct*)malloc(420)
	teststruct* c =b
	((int*)b)[0]=420
	((int*)b)[1]=5021
	ptr:=(long*)malloc(5021)
	ptr[0]=420420420
	((long**)b)[1]=ptr // [1] because long*s are 8 bytes so [1] lines up with the beginning of c in teststruct
	print(c[0].a)
	c[0].a=5
	print(c[0].a)
	print(c[0].b)
	print(c[0].c[0])
	*ptr=*ptr+50215021
	print(c[0].c[0])
	teststruct aoeu=*c
	print(aoeu.a)
	aoeu.a=aoeu.a+46290
	print(aoeu.a)
	aoeu.b=444
	print(aoeu.a)
	print(aoeu.b)
	print((*c).a)
}