package io.virgo.randomX;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * JNA wrapping to the randomX native library
 */
interface Bindings extends Library {

	public static final String JNA_LIBRARY_NAME = "randomx";
	public static final Bindings INSTANCE = loadLib();
	
	int randomx_get_flags();
	
	PointerByReference randomx_alloc_cache(int flags);
	
	void randomx_init_cache(PointerByReference cache, Pointer key, NativeSize keySize);

	PointerByReference randomx_create_vm(int flags, PointerByReference cache, PointerByReference dataset);

	void randomx_calculate_hash(PointerByReference machine, Pointer input, NativeSize inputSize, Pointer output);

	void randomx_release_cache(PointerByReference cache);

	void randomx_destroy_vm(PointerByReference machine);

	void randomx_vm_set_cache(PointerByReference machine, PointerByReference cache);
	
	PointerByReference randomx_alloc_dataset(int flags);
	
	NativeLong randomx_dataset_item_count();
	
	void randomx_init_dataset(PointerByReference dataset, PointerByReference cache, NativeLong startItem, NativeLong itemCount);

	void randomx_release_dataset(PointerByReference dataset);

	void randomx_vm_set_dataset(PointerByReference machine, PointerByReference dataset);

	/**
	 * Extract library from jar to lib/ directory then load it
	 */
	private static Bindings loadLib() {
		String name = System.mapLibraryName(JNA_LIBRARY_NAME); // extends name with .dll, .so or .dylib
		File extractedLib = new File("lib/"+name);
		InputStream inputStream = Bindings.class.getResourceAsStream(name);
		try {
			Files.createDirectories(extractedLib.getParentFile().toPath());
			Files.copy(inputStream, extractedLib.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.setProperty("jna.library.path", extractedLib.getParent());
		return (Bindings)Native.load(JNA_LIBRARY_NAME, Bindings.class);
	}
	
}
