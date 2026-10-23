package com.aizistral.enigmaticlegacy.helpers;

import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import vazkii.patchouli.client.base.PersistentData;
import vazkii.patchouli.client.base.PersistentData.BookData;
import vazkii.patchouli.client.book.BookEntry;
import vazkii.patchouli.common.book.Book;
import vazkii.patchouli.common.book.BookRegistry;

/**
 * Client-only: touches Patchouli's client book data.
 */
public class PatchouliHelper {

	protected static Book getBook(ResourceLocation location) {
		return BookRegistry.INSTANCE.books.get(location);
	}

	public static Book getAknowledgment() {
		// Port: BuiltInRegistries instead of ForgeRegistries.
		return PatchouliHelper.getBook(BuiltInRegistries.ITEM.getKey(EnigmaticItems.THE_ACKNOWLEDGMENT));
	}

	private static void setEntryState(ResourceLocation entryLocation, boolean read) {
		Book theBook = PatchouliHelper.getAknowledgment();

		// Port: null check for the book, in case Patchouli failed to load it.
		if (theBook == null)
			return;

		BookEntry entry = theBook.getContents().entries.get(entryLocation);
		BookData data = PersistentData.data.getBookData(theBook);

		if (data == null || data.viewedEntries == null || entry == null || entry.getId() == null)
			return;

		if (read && !data.viewedEntries.contains(entry.getId())) {
			data.viewedEntries.add(entry.getId());
			entry.markReadStateDirty();
		} else if (!read && data.viewedEntries.contains(entry.getId())) {
			data.viewedEntries.remove(entry.getId());
			entry.markReadStateDirty();
		}
	}

	public static void markEntryUnread(ResourceLocation entryLocation) {
		PatchouliHelper.setEntryState(entryLocation, false);
	}

	public static void markEntryRead(ResourceLocation entryLocation) {
		PatchouliHelper.setEntryState(entryLocation, true);
	}

	public static void markEverythingRead() {
		Book theBook = PatchouliHelper.getAknowledgment();

		for (ResourceLocation location :  theBook.getContents().entries.keySet()) {
			PatchouliHelper.markEntryRead(location);
		}
	}

	public static void markEverythingUnread() {
		Book theBook = PatchouliHelper.getAknowledgment();

		for (ResourceLocation location :  theBook.getContents().entries.keySet()) {
			PatchouliHelper.markEntryUnread(location);
		}
	}

}
