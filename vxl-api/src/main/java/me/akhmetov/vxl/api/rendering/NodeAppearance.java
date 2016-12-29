package me.akhmetov.vxl.api.rendering;

import com.badlogic.gdx.utils.Disposable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Common interface for node appearances. Cannot be constructed directly by plugin classes; plugins must construct
 * one of its subclasses instead.
 */
public abstract class NodeAppearance {
    /**
     * Protected constructor. Until security and usability can be evaluated easily it makes sense to have pre-defined
     * classes for nodes as boxes, nodes as partial boxes, etc. For now, only nodes as boxes are possible.
     */
    protected NodeAppearance(){

    }

    private List<Disposable> disposables = new CopyOnWriteArrayList<Disposable>();

    public void disposeAssets(){
        for(Disposable disp : disposables){
            disp.dispose();
        }
    }

}
