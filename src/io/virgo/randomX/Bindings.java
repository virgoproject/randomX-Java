package io.virgo.randomX;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

interface Bindings extends Library {

	public static final String JNA_LIBRARY_NAME = "randomx";
	public static final NativeLibrary JNA_NATIVE_LIB = NativeLibrary.getInstance(Bindings.JNA_LIBRARY_NAME);
	public static final Bindings INSTANCE = (Bindings)Native.load(Bindings.JNA_LIBRARY_NAME, Bindings.class);
	
	int randomx_get_flags();
	
	PointerByReference randomx_alloc_cache(int flags);
	
	void randomx_init_cache(PointerByReference cache, Pointer key, NativeSize keySize);

	PointerByReference randomx_create_vm(int flags, PointerByReference cache, PointerByReference dataset);

	void randomx_calculate_hash(PointerByReference machine, Pointer input, NativeSize inputSize, Pointer output);

	void randomx_release_cache(PointerByReference cache);

	void randomx_destroy_vm(PointerByReference machine);

	void randomx_vm_set_cache(PointerByReference machine, PointerByReference cache);

}
