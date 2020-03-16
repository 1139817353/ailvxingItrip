package cn.ekgc.itrip.dao;

import cn.ekgc.itrip.pojo.vo.HotelRoom;
import cn.ekgc.itrip.pojo.vo.SearchHotelRoomVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * <b>爱旅行-酒店房间功能数据持久层接口</b>
 */
@Repository
public interface HotelRoomDao {
	/**
	 * <b>根据嘻哈寻条件查询列表信息</b>
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public List<HotelRoom> findListByQuery(HotelRoom query) throws Exception;

	/**
	 * <b>根据查询条件获得此时临时库存数量</b>
	 * @param queryMap
	 * @return
	 * @throws Exception
	 */
	public Integer queryTempStore(Map<String,Object> queryMap) throws Exception;

	/**
	 * <b>根据查询条件查询总库存</b>
	 * @param queryMap
	 * @return
	 * @throws Exception
	 */
     public Integer queryTotalStore(Map<String,Object> queryMap) throws Exception;
}