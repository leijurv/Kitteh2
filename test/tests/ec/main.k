import ./base
import math/bigint

main(){

	x:=basepointX()
	bigint* y=basepointY()
	modulus:=modulus()
	x.output()
	y.output()
	modulus.output()

	bp:=basepoint()
	for i:=15; i<=20; i++{
		print(i)
		pow:=fromLong((long)i)
		pt:=bp.dup().multiply(pow,modulus,bp)
		pt.output()
		pt.free()
		pow.free()
	}
	bp.dup().multiply(fromLong(50215021),modulus,bp).output()
}

struct ecpoint{
	bigint* x
	bigint* y
	output(){
		x:=(this.x).tostring(',')
		writeNullTerm(x)
		(this.y).output()
	}
	free(){
		free(this.x)
		free(this.y)
		this.x=(bigint*)0
		this.y=(bigint*)0
	}
	dup()ecpoint*{
		new:=(ecpoint*)malloc(sizeof(ecpoint))
		new.x=(this.x).dup()
		new.y=(this.y).dup()
		return new
	}
	add(ecpoint* q,bigint* modulus)ecpoint*{
		px:=this.x
		py:=this.y
		qx:=q.x
		qy:=q.y
	    bigint* s
	    if equal(qx,px){
	    	xs:=multiply(px,px)
	    	twoxs:=add(xs,xs)
	    	threexs:=add(xs,twoxs)
	    	(xs).free()
	    	(twoxs).free()
	    	twopy:=add(py,py)
	    	invertedy:=modInverse(twopy,modulus)
	    	(twopy).free()
	    	lmao:=multiply(threexs,invertedy)
	    	(threexs).free()
	    	(invertedy).free()
	    	s=mod(lmao,modulus)
	    	(lmao).free()
	    }else{
	    	ydiff:=subtract(py,qy)
	    	a:=add(modulus,ydiff)
	    	(ydiff).free()
	    	xdiff:=subtract(px,qx)
	    	posxdiff:=add(modulus,xdiff)
	    	(xdiff).free()
	    	b:=modInverse(posxdiff,modulus)
	    	(posxdiff).free()
	    	ab:=multiply(a,b)
	    	(a).free()
	    	(b).free()
	    	s=mod(ab,modulus)
	    	(ab).free()
	    }
	    ss:=multiply(s,s)
	    sspx:=subtract(ss,px)
	    (ss).free()
	    sspxqx:=subtract(sspx,qx)
	    (sspx).free()
	    xr:=mod(sspxqx,modulus)
	    (sspxqx).free()
	    xdiff:=subtract(xr,px)
	    sxdiff:=multiply(s,xdiff)
	    (xdiff).free()
	    pysx:=add(py,sxdiff)
	    (sxdiff).free()
	    negpysx:=subtract(modulus,pysx)
	    (pysx).free()
	    wrap1:=mod(negpysx,modulus)
	    (negpysx).free()
	    bigint* yr
	    if wrap1.neg{
	    	yr=add(modulus,wrap1)
	    	free(wrap1)
	    }else{
	    	yr=wrap1
	    }
	    (s).free()
	    res:=(ecpoint*)malloc(sizeof(ecpoint))
	    res.x=xr
	    res.y=yr
	    return res
	}
	multiply(bigint* pow,bigint* MOD,ecpoint* bp)ecpoint*{
		pow.trimInPlace()
		if pow.size==1 && pow.bit(0){
			return this
		}
		if pow.size<2{
			return (ecpoint*)0
		}
		if pow.bit(0){
			//odd
			
			one:=new(1)
			one.setbit(0,true)
			powminus:=subtract(pow,one)
			one.free()

			min:=this.multiply(powminus,MOD,bp)

			powminus.free()

			res:=bp.add(min,MOD)
			min.free()
			//pow.output()
			return res
		}
		
		shiftedPow:=pow.rightshiftone()
		wew:=this.multiply(shiftedPow,MOD,bp)
		shiftedPow.free()
		wewwew:=wew.add(wew,MOD)
		wew.free()
		//pow.output()
		return wewwew
	}
}