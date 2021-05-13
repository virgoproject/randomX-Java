package io.virgo.randomX;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/*
 * RandomX virtual machine, uses dataset to calculate hashes
 */
public class RandomX_VM {

	PointerByReference pointer;
	RandomX parent;
	
	RandomX_VM(PointerByReference pointer, RandomX parent) {
		this.pointer = pointer;
		this.parent = parent;
	}
	
	/**
	 * Calculate hash of given message
	 * @param message the message to get the hash of
	 * @return the resulting hash
	 */
	public synchronized byte[] getHash(byte[] message) {
		
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
	
	/**
	 * Destroy this VM
	 */
	public void destroy() {
		Bindings.INSTANCE.randomx_destroy_vm(pointer);
		parent.vms.remove(this);
	}
	
}
