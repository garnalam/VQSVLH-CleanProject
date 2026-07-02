package com.vqsv.rebuild.resource;

import com.vqsv.rebuild.core.GameConfig;

import java.nio.file.Files;
import java.nio.file.Path;

public final class AssetPaths {
    private final Path modulesRoot;

    private AssetPaths(Path modulesRoot) {
        this.modulesRoot = modulesRoot;
    }

    public static AssetPaths fromWorkingTree(GameConfig config) {
        return new AssetPaths(config.modulesRoot());
    }

    public Path modulesRoot() {
        return modulesRoot;
    }

    public Path moduleRoot(ResourceKind kind) {
        return modulesRoot.resolve(kind.folderName()).normalize();
    }

    public Path original(ResourceKind kind, String fileName) {
        return moduleRoot(kind).resolve("original").resolve(fileName).normalize();
    }

    public Path decoded(ResourceKind kind, String fileName) {
        return moduleRoot(kind).resolve("decoded").resolve(fileName).normalize();
    }

    public Path manifest(ResourceKind kind) {
        return moduleRoot(kind).resolve("manifest.csv").normalize();
    }

    public Path imgOriginal(int id) {
        return original(ResourceKind.IMG, "img_" + id + ".mid");
    }

    public Path imgOriginalDir() {
        return moduleRoot(ResourceKind.IMG).resolve("original").normalize();
    }

    public Path imgDecodedPng(int id) {
        return decoded(ResourceKind.IMG, "data__img__img_" + id + ".mid.png");
    }

    public Path imgDecodedDir() {
        return moduleRoot(ResourceKind.IMG).resolve("decoded").normalize();
    }

    public Path logoDecodedPng(String name) {
        return modulesRoot.resolve("logo").resolve("decoded").resolve("data__logo__" + name + ".png").normalize();
    }

    public Path logoOriginalPng(String name) {
        return modulesRoot.resolve("logo").resolve("original").resolve(name + ".png").normalize();
    }

    public Path logoCustomPng(String name) {
        return modulesRoot.resolve("logo").resolve("custom").resolve(name + ".png").normalize();
    }

    public Path texDecodedPng(String name) {
        return modulesRoot.resolve("tex").resolve("decoded").resolve("data__tex__" + name + ".png").normalize();
    }

    public Path texOriginalMid(String name) {
        return modulesRoot.resolve("tex").resolve("original").resolve(name + ".mid").normalize();
    }

    public Path sprOriginal(int id) {
        return original(ResourceKind.SPR, "spr_" + id + "_all(r)");
    }

    public Path sprDecodedJson(int id) {
        return decoded(ResourceKind.SPR, "data__spr__spr_" + id + "_all(r).json");
    }

    public Path mapOriginal(int id) {
        return original(ResourceKind.MAP, "map_" + id + ".mid");
    }

    public Path mapDecodedJson(int id) {
        return decoded(ResourceKind.MAP, "data__map__map_" + id + ".mid.json");
    }

    public Path modOriginal(int id) {
        return original(ResourceKind.MOD, "mod_" + id + ".mid");
    }

    public Path modInfoOriginal() {
        return original(ResourceKind.MOD, "modInfo.mid");
    }

    public Path modDecodedJson(int id) {
        return decoded(ResourceKind.MOD, "data__mod__mod_" + id + ".mid.json");
    }

    public Path modInfoDecodedJson() {
        return decoded(ResourceKind.MOD, "data__mod__modInfo.mid.json");
    }

    public Path scriptOriginal(String name) {
        return original(ResourceKind.SCRIPT, name);
    }

    public Path spriteTableOriginal() {
        return scriptOriginal("sprite.mid");
    }

    public Path fontBin() {
        return modulesRoot.resolve("root_misc").resolve("original").resolve("font.bin").normalize();
    }

    public Path uiOriginal(String name) {
        return original(ResourceKind.UI, name);
    }

    public Path ui(String name) {
        return uiOriginal(name);
    }

    public Path eventDecoded(String name) {
        return modulesRoot.resolve("event").resolve("decoded").resolve(name);
    }

    public Path eventOriginalMid(int sceneId) {
        return original(ResourceKind.EVENT, "scene_" + sceneId + ".mid");
    }

    public Path eventOriginalMib(int sceneId) {
        return original(ResourceKind.EVENT, "scene_" + sceneId + ".mib");
    }

    public Path eventDecodedMidJson(int sceneId) {
        return decoded(ResourceKind.EVENT, "data__event__scene_" + sceneId + ".mid.json");
    }

    public Path eventDecodedMibJson(int sceneId) {
        return decoded(ResourceKind.EVENT, "data__event__scene_" + sceneId + ".mib.json");
    }

    public Path sourceCfr(String relativePath) {
        return modulesRoot.resolve("source_code").resolve("decoded").resolve("decompiled_source_cfr").resolve(relativePath);
    }

    public boolean modulesRootExists() {
        return Files.isDirectory(modulesRoot);
    }

    public boolean hasUi(String name) {
        return Files.isRegularFile(ui(name));
    }
}
