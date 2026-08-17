package github.C2F4n.api;

/** 内容变化监听：罐、槽、组件状态变化时回调，驱动配方缓存刷新与保存。 */
@FunctionalInterface
public interface IContentsListener {

    void onContentsChanged();
}
