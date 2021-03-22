package io.virgo.randomX;

import java.util.ArrayList;
import java.util.Arrays;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class RandomX {
	
	public static final int HASH_SIZE = 32;
	
	private PointerByReference cache = null;
	private PointerByReference dataset = null;
	
	ArrayList<RandomX_VM> vms = new ArrayList<RandomX_VM>();
	
	boolean fastInit = false;
	
	private Pointer key;
	private int keySize;
	
	private int flagsValue = 0;
	private ArrayList<Flag> flags;
	
	private RandomX(Builder builder) {	
		
		fastInit = builder.fastInit;
		
		flags = builder.flags;
		if(flags.size() == 0)
			flagsValue = Bindings.INSTANCE.randomx_get_flags();	
		else
			for(Flag flag : flags)
				flagsValue+=flag.getValue();
		
	}

	public void init(byte[] key) {
		
		if(flags.contains(Flag.FULL_MEM))
			setDataSet(key);
		else
			setCache(key);
		
	}
	
	public RandomX_VM createVM() {
		RandomX_VM vm = new RandomX_VM(Bindings.INSTANCE.randomx_create_vm(flagsValue, cache, dataset), this);
		vms.add(vm);
		return vm;
	}
	
	private void setCache(byte[] key) {
		if(this.key != null && Arrays.equals(key, this.key.getByteArray(0, keySize)))
			return;
		
		if(cache != null)
			Bindings.INSTANCE.randomx_release_cache(cache);

		cache = Bindings.INSTANCE.randomx_alloc_cache(flagsValue);
		
		this.key = new Memory(key.length);
		this.key.write(0, key, 0, key.length);
		keySize = key.length;
		
		Bindings.INSTANCE.randomx_init_cache(cache, this.key, new NativeSize(key.length));
	}
	
	private void setDataSet(byte[] key) {
		if(this.key != null && Arrays.equals(key, this.key.getByteArray(0, keySize)))
			return;
		
		setCache(key);
		
		if(dataset != null)
			Bindings.INSTANCE.randomx_release_dataset(dataset);
		
		if(flags.contains(Flag.LARGE_PAGES))
			dataset = Bindings.INSTANCE.randomx_alloc_dataset(Flag.LARGE_PAGES.getValue());
		else
			dataset = Bindings.INSTANCE.randomx_alloc_dataset(0);
		
		if(fastInit) {
			
			ArrayList<Thread> threads = new ArrayList<Thread>();
			int threadCount = Runtime.getRuntime().availableProcessors();
			
			long perThread = Bindings.INSTANCE.randomx_dataset_item_count().longValue() / threadCount;
			long remainder = Bindings.INSTANCE.randomx_dataset_item_count().longValue() % threadCount;
			
			long startItem = 0;
			for (int i = 0; i < threadCount; ++i) {
				long count = perThread + (i == threadCount - 1 ? remainder : 0);
				long start = startItem;
				Thread thread = new Thread(new Runnable() {

					@Override
					public void run() {
						Bindings.INSTANCE.randomx_init_dataset(dataset, cache, new NativeLong(start), new NativeLong(count));
					}
					
				});
				thread.start();
				threads.add(thread);
				startItem += count;
			}
			
			//wait for every thread to terminate execution (ie: dataset is initialised)
			for(Thread thread : threads)
				try {
					thread.join();
				} catch (InterruptedException e) {}
			
		}else Bindings.INSTANCE.randomx_init_dataset(dataset, cache, new NativeLong(0), Bindings.INSTANCE.randomx_dataset_item_count());
		
		
		Bindings.INSTANCE.randomx_release_cache(cache);
	}
	
	public void changeKey(byte[] key) {
				
		if(flags.contains(Flag.FULL_MEM)) {
			setDataSet(key);
			for(RandomX_VM vm : vms)
				Bindings.INSTANCE.randomx_vm_set_dataset(vm.getPointer(), dataset);
		}else {
			setCache(key);
			for(RandomX_VM vm : vms)
				Bindings.INSTANCE.randomx_vm_set_cache(vm.getPointer(), cache);
		}
		
	}
	
	public void destroy() {

		for(RandomX_VM vm : vms) {
			Bindings.INSTANCE.randomx_destroy_vm(vm.getPointer());
		}
		
		vms.clear();
		
		if(cache != null) {
			Bindings.INSTANCE.randomx_release_cache(cache);
			cache = null;
		}
		
		if(dataset != null) {
			Bindings.INSTANCE.randomx_release_dataset(cache);
			dataset = null;
		}

	}
	
	public static class Builder {
		
		private ArrayList<Flag> flags = new ArrayList<Flag>();

		private boolean fastInit = false;
		
		public RandomX build() {
			return new RandomX(this);
		}
		
		public Builder fastInit(boolean value) {
			fastInit = value;
			return this;
		}
		
		public Builder flag(Flag flag) {
			flags.add(flag);
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
