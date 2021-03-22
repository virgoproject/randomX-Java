package io.virgo.randomX;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class RandomX_VM {

	PointerByReference pointer;
	RandomX parent;
	
	RandomX_VM(PointerByReference pointer, RandomX parent) {
		this.pointer = pointer;
		this.parent = parent;
	}
	
	public byte[] getHash(byte[] message) {
		
		Pointer msgPointer = new Memory(message.length);
		msgPointer.write(0, message, 0, message.length);
		
		Pointer hashPointer = new Memory(RandomX.HASH_SIZE);
		Bindings.INSTANCE.randomx_calculate_hash(pointer, msgPointer, new NativeSize(message.length), hashPointer);
		
		byte[] hash = hashPointer.getByteArray(0, RandomX.HASH_SIZE);
		
		msgPointer.clear(message.length);
		hashPointer.clear(RandomX.HASH_SIZE);
		
		return hash;
	}
	
	protected PointerByReference getPointer() {
		return pointer;
	}
	
	public void destroy() {
		Bindings.INSTANCE.randomx_destroy_vm(pointer);
		parent.vms.remove(this);
	}
	
}
