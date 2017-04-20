package org.n52.sos.ogc.gml;

import java.util.List;

import org.n52.sos.w3c.xlink.Referenceable;

/**
 * Internal representation of the OGC GML VerticalCS.
 * 
 * @author Carsten Hollmann <c.hollmann@52north.org>
 * @since 4.4.0
 *
 */
public class VerticalCS extends AbstractCoordinateSystem {

    public VerticalCS(CodeWithAuthority identifier, Referenceable<CoordinateSystemAxis> coordinateSystemAxis) {
        super(identifier, coordinateSystemAxis);
    }
    
    public VerticalCS(CodeWithAuthority identifier, List<Referenceable<CoordinateSystemAxis>> coordinateSystemAxis) {
        super(identifier, coordinateSystemAxis);
    }

    private static final long serialVersionUID = 9109947004063278729L;

}