import ../bigint/main b
main(){
	twofivesix:=b::fromLong(1)
	b::leftshift(twofivesix,256)
	thirtytwo:=b::fromLong(1)
	b::leftshift(thirtytwo,32)
	wew:=b::sub(twofivesix,thirtytwo)
	ninesevenseven:=b::fromLong(977)
	modulus:=b::sub(wew,ninesevenseven)
	b::f(twofivesix)
	b::f(thirtytwo)
	b::f(wew)
	b::f(ninesevenseven)
	



	b::output(modulus)
}
