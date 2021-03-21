package io.virgo.randomX;

import java.util.Arrays;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class RandomX {
	
	public static final int HASH_SIZE = 32;
	
	private PointerByReference cache;
	private PointerByReference vm;
	
	private Pointer key;
	private int keySize;
	
	
	private int flags = 0;
	
	private RandomX(Builder builder) {
		
		flags = builder.flags;
		if(flags == 0)
			flags = Bindings.INSTANCE.randomx_get_flags();	
		
	}

	public void init(byte[] key) {
		assert vm == null;
		
		setCache(key);
		vm = Bindings.INSTANCE.randomx_create_vm(flags, cache, null);
	}
	
	public byte[] getHash(byte[] message) {
		assert vm != null;
		
		Pointer msgPointer = new Memory(message.length);
		msgPointer.write(0, message, 0, message.length);
		
		Pointer hashPointer = new Memory(HASH_SIZE);
		Bindings.INSTANCE.randomx_calculate_hash(vm, msgPointer, new NativeSize(message.length), hashPointer);
		
		byte[] hash = hashPointer.getByteArray(0, HASH_SIZE);
		
		msgPointer.clear(message.length);
		hashPointer.clear(HASH_SIZE);
		
		return hash;
	}
	
	private void setCache(byte[] key) {
		if(this.key != null && Arrays.equals(key, this.key.getByteArray(0, keySize)))
			return;
		
		if(cache != null)
			Bindings.INSTANCE.randomx_release_cache(cache);

		cache = Bindings.INSTANCE.randomx_alloc_cache(flags);
		
		this.key = new Memory(key.length);
		this.key.write(0, key, 0, key.length);
		keySize = key.length;
		
		Bindings.INSTANCE.randomx_init_cache(cache, this.key, new NativeSize(key.length));
	}
	
	public void changeKey(byte[] key) {
		if(vm == null) {
			init(key);
			return;
		}
				
		setCache(key);
		Bindings.INSTANCE.randomx_vm_set_cache(vm, cache);
	}
	
	public void destroy() {
		assert vm != null && cache != null;
		
		Bindings.INSTANCE.randomx_destroy_vm(vm);
		Bindings.INSTANCE.randomx_release_cache(cache);
		vm = null;
		cache = null;
	}
	
	public static class Builder {
		
		int flags = 0;

		public RandomX build() {
			return new RandomX(this);
		}
		
		public Builder flag(Flag flag) {
			flags+=flag.getValue();
			return this;
		}
	}
	
	public enum Flag {

		DEFAULT(0),
		LARGE_PAGES(1),
		HARD_AES(2),
		FULL_MEM(4),
		JIT(8),
		SECURE(16),
		ARGON2_SSSE3(32),
		ARGON2_AVX2(64),
		ARGON2(96);
		
		private int value;
		
		Flag(int value){
			this.value = value;
		}

		public int getValue() {
			return value;
		}
		
	}
}
