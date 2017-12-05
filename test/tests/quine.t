main(){
a:="main(){&a:=&for i:=0; i<11; i++{&p(a[i])&}&writeByte(34)&print(a)&writeByte(34)&for i:=11; a[i]!=(byte)0; i++{&p(a[i])&}&}&p(byte x){&if x==(byte)38{&writeByte(10)&}else{&writeByte(x)&}&}"
for i:=0; i<11; i++{
p(a[i])
}
writeByte(34)
print(a)
writeByte(34)
for i:=11; a[i]!=(byte)0; i++{
p(a[i])
}
}
p(byte x){
if x==(byte)38{
writeByte(10)
}else{
writeByte(x)
}
}