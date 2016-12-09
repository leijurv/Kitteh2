import base
struct ecpoint{
	bigint* x
	bigint* y
}
main(){
	x:=basepointX()
	bigint* y=basepointY()
	modulus:=modulus()
	output(x)
	output(y)
	output(modulus)

	bp:=(ecpoint*)malloc(sizeof(ecpoint))
	bp.x=x
	bp.y=y

	for i:=15; i<=20; i++{
		print(i)
		pow:=fromLong((long)i)
		pt:=multiplyec(dupec(bp),pow,modulus,bp)
		outputec(pt)
		clear(pt)
		f(pow)
	}
	outputec(multiplyec(dupec(bp),fromLong(50215021),modulus,bp))
}
clear(ecpoint* a){
	f(a.x)
	f(a.y)
	a.x=(bigint*)0
	a.y=(bigint*)0
	free(a)
}
dupec(ecpoint* x)ecpoint*{
	new:=(ecpoint*)malloc(sizeof(ecpoint))
	new.x=dup(x.x)
	new.y=dup(x.y)
	return new
}
outputec(ecpoint* t){
	x:=tostring(t.x,',')
	writeNullTerm(x)
	output(t.y)
}
multiplyec(ecpoint* base,bigint* pow,bigint* MOD,ecpoint* bp)ecpoint*{
	trimInPlace(pow)
	if pow.size==1 && pow.cont[0]{
		return base
	}
	if pow.size<2{
		return (ecpoint*)0
	}
	if pow.cont[0]{
		//odd
		
		one:=new(1)
		one.cont[0]=true
		powminus:=sub(pow,one)
		f(one)

		min:=multiplyec(base,powminus,MOD,bp)

		f(powminus)

		res:=addec(bp,min,MOD)
		clear(min)

		return res
	}
	
	shiftedPow:=rightshiftone(pow)
	wew:=multiplyec(base,shiftedPow,MOD,bp)
	f(shiftedPow)
	wewwew:=addec(wew,wew,MOD)
	clear(wew)
	return wewwew
}
addec(ecpoint* p,ecpoint* q,bigint* modulus)ecpoint*{
	px:=p.x
	py:=p.y
	qx:=q.x
	qy:=q.y
    bigint* s
    if equal(qx,px){
    	xs:=multiply(px,px)
    	twoxs:=add(xs,xs)
    	threexs:=add(xs,twoxs)
    	f(xs)
    	f(twoxs)
    	twopy:=add(py,py)
    	invertedy:=modInverse(twopy,modulus)
    	f(twopy)
    	lmao:=multiply(threexs,invertedy)
    	f(threexs)
    	f(invertedy)
    	s=mod(lmao,modulus)
    	f(lmao)
    }else{
    	ydiff:=sub(py,qy)
    	a:=add(modulus,ydiff)
    	f(ydiff)
    	xdiff:=sub(px,qx)
    	posxdiff:=add(modulus,xdiff)
    	f(xdiff)
    	b:=modInverse(posxdiff,modulus)
    	f(posxdiff)
    	ab:=multiply(a,b)
    	f(a)
    	f(b)
    	s=mod(ab,modulus)
    	f(ab)
    }
    ss:=multiply(s,s)
    sspx:=sub(ss,px)
    f(ss)
    sspxqx:=sub(sspx,qx)
    f(sspx)
    xr:=mod(sspxqx,modulus)
    f(sspxqx)
    xdiff:=sub(xr,px)
    sxdiff:=multiply(s,xdiff)
    f(xdiff)
    pysx:=add(py,sxdiff)
    f(sxdiff)
    negpysx:=sub(modulus,pysx)
    f(pysx)
    wrap1:=mod(negpysx,modulus)
    f(negpysx)
    wrap2:=add(modulus,wrap1)
    f(wrap1)
    yr:=mod(wrap2,modulus)
    f(wrap2)
    f(s)
    res:=(ecpoint*)malloc(sizeof(ecpoint))
    res.x=xr
    res.y=yr
    return res
}
